package huyang.edu.cn;

import huyang.edu.cn.Utils.TwordsComparable;
import huyang.edu.cn.Utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Model {

    private static final Logger log = LoggerFactory.getLogger(Model.class);

    /**
     * V is word num
     */
    private int V;

    /**
     * K is topic num
     */
    private int K;

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
     * phi is topiic--words 矩阵  K*V
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

    /**
     * 迭代次数
     */
    private int iterations;

    /**
     * 需要进行模型保存的迭代次数
     */
    private int saveStepNum;

    /**
     * 开始保存模型的当前迭代数
     */
    private int beginSave;

    private int topNum=20;

    private Map<String, Integer> wordToIndex;

    /**
     * @param wordToIndex
     * @param matrix M*N
     */
    public Model(Map<String, Integer> wordToIndex, Matrix matrix) {
        this.K = 8;
        this.alpha = 50f/this.K;
        this.beta = 0.1f;
        this.iterations = 500;
        this.beginSave = 80;
        this.saveStepNum = 10;
        this.wordToIndex = wordToIndex;
        initModel(wordToIndex, matrix);
    }

    private void initModel(Map<String, Integer> wordToIndex, Matrix<Integer> matrix) {
        log.info("init model");
        this.M = matrix.getSize();
        this.V = wordToIndex.keySet().size();
        this.theta = new Matrix<Double>(M,K,0d);
        this.phi = new Matrix<Double>(K,V,0d);
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

    public void trainModel(Matrix matrix,double threshold) throws Exception {
        for(int i = 0; i < iterations; i++) {
            log.info("iterationNum is "+i);
            System.out.println("iterationNum is "+i);
//            Matrix temp = phi;

//            if(isConvergence(temp,phi,threshold)) {
//                saveIteratedModel(i+1,"result.txt");
//                break;
//            }
            //进行吉布斯采样
            for(int m = 0; m < M; m++) {
                Vector<Integer> doc = matrix.getValue(m);
                int N = doc.getSize();
                for(int n = 0; n < N; n++) {
                    log.info("采样: m:"+m+"  n:"+n);
                    int newTopic = sampleNewTopic(m,n,doc);
                    z.getValue(m).setValue(n, newTopic);
                }
            }
            if(i==iterations-1) {
                updateEstimatedParameters();
                saveIteratedModel(i+1,"result.txt");
            }
        }

    }

    private void updateEstimatedParameters() {
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

    private int sampleNewTopic(int m, int n, Vector<Integer> doc) {
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

        for(int k = 1; k < K; k++){
            p[k] += p[k - 1];
        }
        double u = Math.random() * p[K - 1]; //p[] is unnormalised
        int newTopic;
        //p当得到当前词属于所有主题z的概率分布后，根据这个概率分布为该词sample一个新的主题，简单通过随机采样选择一个topic
        for(newTopic = 0; newTopic < K; newTopic++){
            if(u < p[newTopic]){
                break;
            }
        }
        m_topic_count.getValue(m).setValue(newTopic,m_topic_count.getValue(m).getValue(newTopic)+1);
        //第K个topic下，第n个单词对应下标的计数
        topic_word_count.getValue(newTopic).setValue(doc.getValue(n),topic_word_count.getValue(newTopic).getValue(doc.getValue(n))+1);
        //对应的topic计数
        topic_word_count_sum.setValue(newTopic,topic_word_count_sum.getValue(newTopic)+1);
        //对应文档的单词计数
        m_topic_count_sum.setValue(m,m_topic_count_sum.getValue(m)+1);
        return newTopic;
    }

    private boolean isConvergence(Matrix<Double> A, Matrix<Double> B,double threshold) {
        double variance = 0;
        assert (A.getSize() == B.getSize());
        for(int i = 0; i < A.getSize(); i++) {
            assert (A.getValue(i).getSize() == B.getValue(i).getSize());
            for(int j = 0; j < A.getValue(i).getSize(); j++) {
                variance += Math.abs(A.getValue(i).getValue(j)-B.getValue(i).getValue(j));
            }
        }
        if(variance <= threshold) {
            return true;
        } else {
            return false;
        }
    }

    private void saveIteratedModel(int i, String output) throws Exception{
        BufferedWriter writer;
        File file = new File(output);
        if(file.exists()) {
            if(file.isDirectory()) {
                writer = new BufferedWriter(new FileWriter(output+"/result"));
            } else {
                file.delete();
                writer = new BufferedWriter(new FileWriter(output));
            }
        } else {
            file.createNewFile();
            writer = new BufferedWriter(new FileWriter(output));
        }
        writer.write("Iterations is "+i);
        writer.write("\n");
        Util util = new Util<String,Integer>();
        for(int k = 0; k < K; k++){
            List<Integer> tWordsIndexArray = new ArrayList<Integer>();
            for(int j = 0; j < V; j++){
                tWordsIndexArray.add(new Integer(j));
            }
            Collections.sort(tWordsIndexArray, new TwordsComparable(phi.getValue(k)));
            writer.write("topic " + k + "\t:\t");
            for(int t = 0; t < topNum; t++){
                writer.write(util.getKey(wordToIndex,tWordsIndexArray.get(t))  + " " + phi.getValue(k).getValue(tWordsIndexArray.get(t)) + "\t");
            }
            writer.write("\n");
        }
        writer.close();
    }

}
