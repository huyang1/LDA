package huyang.edu.cn.model;

import huyang.edu.cn.Utils.MatrixOperations;
import huyang.edu.cn.Utils.Util;
import huyang.edu.cn.vector.Matrix;
import huyang.edu.cn.vector.Param;
import huyang.edu.cn.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class LDAModel extends Model{

    private static final Logger log = LoggerFactory.getLogger(LDAModel.class);

    /**
     * V is word num
     */
    private int V;

    /**
     * M is docement num
     */
    private int M;

    /**
     * z is topic label M*N
     */
    private Matrix<Integer> z;

    /**
     * alpha 是 theta 先验分布参数
     */
    private float alpha;

    /**
     * theta is doc--topic 矩阵 M*K
     */
    private Matrix<Double> theta;

    /**
     * beta 是 phi 先验分布参数
     */
    private float beta;

    /**
     * phi is topic--words 矩阵  K*V
     */
    private Matrix<Double> phi;

    /**
     * m_topic_count 是 第m篇文档主题K下面word的计数 M*K
     */
    private Matrix<Integer> m_topic_count;

    /**
     * 主题K中单词t出现的次数  K*V
     */
    private Matrix<Integer> topic_word_count;

    /**
     * 每篇文档中所有topic下单词的总和，即文档中单词的个数   M
     */
    private Vector<Integer> m_topic_count_sum;

    /**
     * 主题K下面每个单词出现的次数   K
     */
    private Vector<Integer> topic_word_count_sum;

    private Matrix<Double> theraSum;

    private Matrix<Double> phiSum;

    private String outputPath;

    /**
     * @param wordToIndex
     * @param matrix M*N
     */
    public LDAModel(Map<String, Integer> wordToIndex, Matrix matrix) {
        super(8,100, 99, 1,"reuslt.txt",10, wordToIndex, matrix);
        this.alpha = 50f/this.K;
        this.beta = 0.01f;

    }

    public LDAModel(Map<String, Integer> wordToIndex, Matrix matrix,
                    String outputPath,
                    int K,
                    int beginSave,
                    int saveNum,
                    int iterations) {
        super(K,iterations, beginSave, saveNum,outputPath,10, wordToIndex, matrix);
        this.alpha = 50f/this.K;
        this.beta = 0.01f;
    }

    @Override
    protected void initModel() {
        super.initModel();
        log.info("init model");
        this.M = matrix.getSize();
        this.V = wordToIndex.keySet().size();
        this.theta = new Matrix<Double>(M,K,0d);
        this.phi = new Matrix<Double>(K,V,0d);
        this.theraSum = new Matrix<Double>(M,K,0d);
        this.phiSum = new Matrix<Double>(K,V,0d);
        this.m_topic_count = new Matrix<Integer>(M,K,0);
        this.topic_word_count = new Matrix<Integer>(K,V,0);
        this.m_topic_count_sum = new Param<Integer>(M,0);
        this.topic_word_count_sum = new Param<Integer>(K,0);

        //由于每个文档单词数不一致，故分开初始化与N有关的变量。
        this.z = new Matrix<Integer>();
        for(int m = 0; m < M; m++) {
            Vector<Integer> doc = matrix.getValue(m);
            int N = doc.getSize();
            Param<Integer> m_topic = new Param<Integer>(N,0);//这篇文章所有单词对应的topic
            for(int n = 0; n < N; n++) {
                int wordIndex = doc.getValue(n);
                int initTopic = (int) (Math.random()*K);
                m_topic.setValue(n, initTopic);
                //第m篇文章下第initTopic个topic的计数+1
                m_topic_count.getValue(m).setValue(initTopic,m_topic_count.getValue(m).getValue(initTopic)+1);
                //第K个topic下，第n个单词对应下标的计数+1
                topic_word_count.getValue(initTopic).setValue(wordIndex,topic_word_count.getValue(initTopic).getValue(wordIndex)+1);
                //对应的topic计数+1
                topic_word_count_sum.setValue(initTopic,topic_word_count_sum.getValue(initTopic)+1);
            }
            m_topic_count_sum.setValue(m,N);
            z.add(m_topic);
        }
    }

    @Override
    public void trainModel() {
        initModel();
        for(int i = 0; i < iterations; i++) {
            log.info("iterationNum is "+i);
            System.out.println("iterationNum is "+i);
            if(i>=beginSave) {
                estimatedParam();
                //将模型参数累加求均值，使结果更加真实
                theraSum = MatrixOperations.plus(theraSum,theta);
                phiSum = MatrixOperations.plus(phiSum, phi);
            }
            if(i==beginSave+saveStepNum-1) {
                theta = MatrixOperations.div(theraSum, saveStepNum);
                phi = MatrixOperations.div(phiSum, saveStepNum);
                try {
                    saveIteratedModel(i+1, phi);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            if(isConvergence(temp,phi,threshold)) {
//                saveIteratedModel(i+1,"result.txt");
//                break;
//            }
            //进行吉布斯采样
            for(int m = 0; m < M; m++) {
                Vector<Integer> doc = matrix.getValue(m);
                int N = doc.getSize();
                for(int n = 0; n < N; n++) {
                    int newTopic = gibbs(m,n,doc);
                    z.getValue(m).setValue(n, newTopic);
                }
            }
            if(i==iterations-1) {
                estimatedParam();
                try {
                    saveIteratedModel(i+1, phi);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.trainModel();
    }

    @Override
    protected void estimatedParam() {
        // TODO Auto-generated method stub
        for(int k = 0; k < K; k++){
            for(int t = 0; t < V; t++){
                phi.getValue(k).setValue(t,(double)(topic_word_count.getValue(k).getValue(t) + beta) / (topic_word_count_sum.getValue(k) + V * beta));
            }
        }

        for(int m = 0; m < M; m++){
            for(int k = 0; k < K; k++){
                theta.getValue(m).setValue(k,(double)(m_topic_count.getValue(m).getValue(k) + alpha) / (m_topic_count_sum.getValue(m) + K * alpha));
            }
        }
    }

    @Override
    protected int gibbs(int m, int n, Vector<Integer> doc) {
        int oldTopic = z.getValue(m).getValue(n);
        int wordIndex = doc.getValue(n);
        //第m篇文章下第initTopic个topic的计数-1
        m_topic_count.getValue(m).setValue(oldTopic,m_topic_count.getValue(m).getValue(oldTopic)-1);
        //第K个topic下，第n个单词对应下标的计数-1
        topic_word_count.getValue(oldTopic).setValue(wordIndex,topic_word_count.getValue(oldTopic).getValue(wordIndex)-1);
        //对应的topic计数-1
        topic_word_count_sum.setValue(oldTopic,topic_word_count_sum.getValue(oldTopic)-1);
        //对应文档的单词计数-1
        m_topic_count_sum.setValue(m,m_topic_count_sum.getValue(m)-1);
        //在给定的样本下计算当前单词topic的概率分布。
        //Compute p(z_i = k|z_-i, w)
        double[] p = new double[K];

        for(int k = 0; k < K; k++) {
            p[k] = (m_topic_count.getValue(m).getValue(k)+alpha)/(m_topic_count_sum.getValue(m)+K*alpha)
                    *(topic_word_count.getValue(k).getValue(wordIndex)+beta)/(topic_word_count_sum.getValue(k)+V*beta);
        }

        int newTopic = Util.sample(p);
        m_topic_count.getValue(m).setValue(newTopic,m_topic_count.getValue(m).getValue(newTopic)+1);
        //第K个topic下，第n个单词对应下标的计数
        topic_word_count.getValue(newTopic).setValue(doc.getValue(n),topic_word_count.getValue(newTopic).getValue(doc.getValue(n))+1);
        //对应的topic计数
        topic_word_count_sum.setValue(newTopic,topic_word_count_sum.getValue(newTopic)+1);
        //对应文档的单词计数
        m_topic_count_sum.setValue(m,m_topic_count_sum.getValue(m)+1);
        return newTopic;
    }

}
