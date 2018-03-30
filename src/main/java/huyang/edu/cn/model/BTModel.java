package huyang.edu.cn.model;

import huyang.edu.cn.Utils.MatrixOperations;
import huyang.edu.cn.Utils.Util;
import huyang.edu.cn.vector.Matrix;
import huyang.edu.cn.vector.Param;
import huyang.edu.cn.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.cs.ext.MacRoman;

import java.util.ArrayList;
import java.util.Map;

public class BTModel extends Model {

    private Logger log = LoggerFactory.getLogger(BTModel.class);

    /**
     * B*2词对
     */
    private Matrix<Integer> biterms;

    private ArrayList<Matrix<Integer>> docBiterms;

    /**
     * z is topic label B
     */
    private Vector<Integer> z;

    private int B;

    private int V;

    /**
     * 主题K中单词t出现的次数  K*V
     */
    private Matrix<Integer> topic_word_count;

    /**
     * topic下词对出现个数
     */
    private Vector<Integer> topic_biterms;

    /**
     * 主题K下面单词出现的次数   K
     */
    private Vector<Integer> topic_word_count_sum;

    /**
     * alpha is K vector
     */
    private Vector<Double> alpha;

    /**
     * K*V超参数
     */
    private Matrix<Double> beta;

    private Vector<Double> beta_topic_sum;

    private Matrix<Double> phi;

    private Vector<Double> theta;

    public BTModel(Map<String, Integer> wordToIndex, Matrix matrix) {
        super(8,100, 99, 1,"BTM/reuslt.txt",10, wordToIndex, matrix);
        this.V = wordToIndex.size();
        this.alpha = new Param<Double>(8,50d/8);
        this.beta = new Matrix<Double>(8, this.V, 0.01);
        this.beta_topic_sum = new Param<Double>(K,this.V*0.01);
        this.phi = new Matrix<Double>(K, V, 0d);
        this.theta = new Param<Double>(K,.0);
        initModel();
    }

    public BTModel(Map<String, Integer> wordToIndex, Matrix matrix,
                   String outputPath,
                   int K,
                   int beginSave,
                   int saveNum,
                   int iterations) {
        super(K,iterations, beginSave, saveNum,outputPath,10, wordToIndex, matrix);
        this.V = wordToIndex.size();
        this.alpha = new Param<Double>(K,50d/8);
        this.beta = new Matrix<Double>(K, this.V, 0.01);
        this.beta_topic_sum = new Param<Double>(K,this.V*0.01);
        this.phi = new Matrix<Double>(K, V, 0d);
        this.theta = new Param<Double>(K,.0);
        initModel();
    }

    @Override
    protected void initModel() {
        super.initModel();
        this.biterms = new Matrix<Integer>();
        this.z = new Param<Integer>();
        this.topic_word_count = new Matrix<Integer>(K, V,0);
        this.topic_biterms = new Param<Integer>(K, 0);
        this.topic_word_count_sum = new Param<Integer>(K, 0);
        //默认词对窗口为10
        for(int i = 0; i < matrix.getSize(); i++) {
            Vector<Integer> doc = matrix.getValue(i);
            for(int j = 0; j < doc.getSize()-1; j++) {
                for(int k = j+1; k < Math.min(doc.getSize(), j+10); k++) {
                    int initTopic = (int)(Math.random()*K);
                    int first = doc.getValue(j);
                    int second = doc.getValue(k);
                    Param<Integer> biterm = new Param<Integer>();
                    biterm.add(first);
                    biterm.add(second);

                    biterms.add(biterm);
                    z.add(initTopic);
                    topic_word_count.getValue(initTopic).setValue(first,topic_word_count.getValue(initTopic).getValue(first)+1);
                    topic_word_count.getValue(initTopic).setValue(second,topic_word_count.getValue(initTopic).getValue(second)+1);
                    topic_biterms.setValue(initTopic, topic_biterms.getValue(initTopic)+1);
                    topic_word_count_sum.setValue(initTopic, topic_word_count_sum.getValue(initTopic)+2);
                }
            }
        }
        this.B = biterms.getSize();
        generateBiterms();
    }

    @Override
    public void trainModel() {
        for(int i = 0; i < iterations; i++) {
            System.out.println("iteration in:"+i);
            if(i>=beginSave) {
                estimatedParam();
                try {
                    saveIteratedModel(i+1, phi);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for(int b = 0; b < B; b++) {
                int newTopic = gibbs(0, b, biterms.getValue(b));
                z.setValue(b, newTopic);
            }

        }
        super.trainModel();
    }

    @Override
    protected void estimatedParam() {
        //开始计算phi
        for(int k = 0; k < K; k++) {
            for(int v = 0; v < V; v++) {
                phi.getValue(k).setValue(v, topic_word_count.getValue(k).getValue(v) /
                        (topic_word_count_sum.getValue(k) + beta_topic_sum.getValue(k)));
            }
        }
        //计算theta
        double alphaSum = .0;
        for(int i =0; i < alpha.getSize(); i++) {
            alphaSum += alpha.getValue(i);
        }
        for(int k = 0; k < K; k++) {
            theta.setValue(k, (topic_biterms.getValue(k)+alpha.getValue(k))/(B+alphaSum));
        }

    }

    @Override
    protected int gibbs(int m, int n, Vector<Integer> B) {
        int oldTopic = z.getValue(n);
        int first = B.getValue(0);
        int second = B.getValue(1);
        topic_word_count.getValue(oldTopic).setValue(first,topic_word_count.getValue(oldTopic).getValue(first)-1);
        topic_word_count.getValue(oldTopic).setValue(second,topic_word_count.getValue(oldTopic).getValue(second)-1);
        topic_biterms.setValue(oldTopic, topic_biterms.getValue(oldTopic)-1);
        topic_word_count_sum.setValue(oldTopic, topic_word_count_sum.getValue(oldTopic)-2);

        //计算topic的概率密度
        double[] p = new double[K];
        for(int k = 0; k < K; k++) {
            p[k] = (alpha.getValue(k)+topic_biterms.getValue(k)) * ((topic_word_count.getValue(k).getValue(first)) +
                    beta.getValue(k).getValue(first)) * ((topic_word_count.getValue(k).getValue(second)) +
                    beta.getValue(k).getValue(second)) / (topic_word_count_sum.getValue(k) + beta_topic_sum.getValue(k)) /
                    (topic_word_count_sum.getValue(k) + beta_topic_sum.getValue(k)+1);
        }
        int newTopic = Util.sample(p);

        topic_word_count.getValue(newTopic).setValue(first,topic_word_count.getValue(newTopic).getValue(first)+1);
        topic_word_count.getValue(newTopic).setValue(second,topic_word_count.getValue(newTopic).getValue(second)+1);
        topic_biterms.setValue(newTopic, topic_biterms.getValue(newTopic)+1);
        topic_word_count_sum.setValue(newTopic, topic_word_count_sum.getValue(newTopic)+2);

        return newTopic;
    }

    public Vector<Double> predictionDoc(int doc) {
        Matrix<Integer> docBiterm = this.docBiterms.get(doc);
        Param<Double> topicPro = new Param<Double>();
        for(int k = 0; k < K; k++) {
            double pro = .0;
            for(int j = 0; j < docBiterm.getSize(); j++) {
                Param<Integer> biterm = (Param<Integer>) docBiterm.getValue(j);
                int first = biterm.getValue(0);
                int second = biterm.getValue(1);
                double a = docBiterm.count(biterm)*1.0/docBiterm.getSize();
                double b = .0;
                for(int i = 0; i < K; i++) {

                    b += this.theta.getValue(i)*this.phi.getValue(i).getValue(first)*this.phi.getValue(i).getValue(second);
                }
                pro += this.theta.getValue(k)*this.phi.getValue(k).getValue(first)*this.phi.getValue(k).getValue(second)
                        / b * a;
            }
            topicPro.add(pro);
        }
        return topicPro;
    }

    public void generateBiterms() {
        this.docBiterms = new ArrayList<>();
        for(int i = 0; i < matrix.getSize(); i++) {
            Matrix<Integer> docBiterm = new Matrix<>();
            Vector<Integer> doc = matrix.getValue(i);
            ArrayList<Integer> words = new ArrayList<>();
            ArrayList<Integer> times = new ArrayList<>();
            for(int j = 0; j < doc.getSize(); j++) {
                int word = doc.getValue(j);
                if(words.contains(word)) {
                    times.set(words.indexOf(word),times.get(words.indexOf(word))+1);
                } else {
                    words.add(word);
                    times.add(1);
                }
            }
            for(int first : words.subList(0,words.size()-1)) {
                for(int second : words.subList(words.indexOf(first)+1,words.size())) {
                    Param<Integer> biterm = new Param<Integer>();
                    biterm.add(first);
                    biterm.add(second);
                    int firstTimes = words.indexOf(first);
                    int secondTimes = words.indexOf(second);
                    if(firstTimes > secondTimes) {
                        for(int time = 0; time < firstTimes; time++) {
                            docBiterm.add(biterm);
                        }
                    } else {
                        for(int time = 0; time < secondTimes; time++) {
                            docBiterm.add(biterm);
                        }
                    }
                }
            }
            this.docBiterms.add(docBiterm);
        }
    }

    @Override
    public Matrix<Double> getPhi() {
        return this.phi;
    }

    @Override
    public Vector<Double> getTheta() {
        return this.theta;
    }

    public Vector<Integer> getTopic_biterms() {
        return topic_biterms;
    }

    public Matrix<Integer> getTopic_word_count() {
        return topic_word_count;
    }

    public void setAlpha(Vector<Double> alpha) {
        this.alpha = alpha;
    }

    public void setBeta(Matrix<Double> beta) {
        this.beta = beta;
    }

    public Vector<Double> getAlpha() {
        return alpha;
    }

    public Matrix<Double> getBeta() {
        return beta;
    }
}
