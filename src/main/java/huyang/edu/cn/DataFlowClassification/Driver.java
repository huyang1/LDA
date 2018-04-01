package huyang.edu.cn.DataFlowClassification;
/**
 * 注：在进行alpha beta 遗传时，默认在每个block中不会有新词的出现
 */

import huyang.edu.cn.Utils.TwordsComparable;
import huyang.edu.cn.model.BTModel;
import huyang.edu.cn.model.LDAModel;
import huyang.edu.cn.vector.Matrix;
import huyang.edu.cn.vector.Param;
import huyang.edu.cn.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;

public class Driver {

    private static final  Logger log = LoggerFactory.getLogger(Driver.class);

    private int dataBlockLength;

    Map<String, Integer> wordToIndex;

    ArrayList<Matrix> blocks;

    private int topicN = 5;

    private int K = 8;

    private int Q = 2;

    private ArrayList<Matrix<Double>> Win;

    private ArrayList<Double> CD;

    private double Threshold = .0;

    private double Lambda = 0.5;

    private ArrayList<List<Integer>> topNword = new ArrayList<>();

    private double[] A = {0, 0.07, 0.1, 0.2, 0.5, 1};

    private int[] F = {0, 1, 2, 3, 4};

    private Vector<Double> alphaInherited;

    private Matrix<Double> betaInherited;

    private SVM svm;

    private ArrayList<Double> labels;

    public Driver(Map<String, Integer> wordToIndex, Matrix dataFlow, int dataBlockLength, int topicN) {
        this.wordToIndex = wordToIndex;
        this.dataBlockLength = dataBlockLength;
        this.topicN = topicN;
        this.blocks = dataFlow.Block(dataBlockLength);
        this.alphaInherited = new Param<Double>(K,.5);
        this.betaInherited = new Matrix<Double>(K, this.wordToIndex.size(), .001);
        this.Win = new ArrayList<Matrix<Double>>();
        this.labels = new ArrayList<Double>();
        /**
         * 将文本标签读入labels中
         */
    }

    public void train() {
        for(Matrix block : blocks) {
            LDAModel ldaModel = new LDAModel(wordToIndex, block);
            ldaModel.trainModel();
            Vector<Vector<Double>> docTopic = ldaModel.getTheta();
            Matrix topicWord = ldaModel.getPhi();

            for(int k = 0; k < K; k++) {
                List<Integer> tWordsIndexArray = new ArrayList<Integer>();
                for(int j = 0; j < wordToIndex.size(); j++){
                    tWordsIndexArray.add(new Integer(j));
                }
                Collections.sort(tWordsIndexArray, new TwordsComparable(topicWord.getValue(k)));
                this.topNword.add(tWordsIndexArray.subList(0,topicN));
            }

            for(int doc = 0; doc < dataBlockLength; doc++) {
                ExtendBlock(docTopic.getValue(doc), doc, block);
            }

            /**
             * 为每个数据块构建onlineBTM model
             */
            BTModel btModel = new BTModel(wordToIndex, block);
            btModel.setBeta(this.betaInherited);
            btModel.setAlpha(this.alphaInherited);
            btModel.trainModel();
            /**
             * 遗传先验参数
             */
            geneticPrioriParam(btModel.getTopic_biterms(),btModel.getTopic_word_count());
            /**
             * 计算数据块中每个短文本的主题概率
             */
            Matrix<Double> data = new Matrix<Double>();
            for(int i = 0; i < block.getSize(); i++) {
                data.add(btModel.predictionDoc(i));
            }
            /**
             * 构建SVM分类器
             */
            if(this.Win.size() == 1) {
                Win.add(data);
                this.svm = new SVM(Win, labels);
                svm.train();
            } else if (this.Win.size() < Q) {
                Win.add(data);
                labels.addAll(Arrays.asList(this.svm.predict(data)));
                this.svm = new SVM(Win, labels);
                svm.train();
            } else {
                /**
                 * 判断是否发现概念漂移，并更新分类器
                 */
                this.CD = new ArrayList<>();
                for(int i = 0; i < Win.size(); i++) {
                    double distance = dist(data, Win.get(i), labels.subList(dataBlockLength*i, dataBlockLength*(i+1)));
                    if(distance > this.Threshold) {
                        CD.add(distance);
                    } else {
                        CD.add(.0);
                    }
                }
                double maxDis = Double.MIN_VALUE;
                int pos = 0;
                for (int i = 0; i < CD.size(); i++) {
                    if(CD.get(i) > maxDis) {
                        maxDis = CD.get(i);
                        pos = i;
                    }
                }
                if(maxDis == .0) {
                    log.info("未发生概念漂移");
                    System.out.println("未发生概念漂移");
                } else {
                    log.info("发生概念漂移");
                    System.out.println("发生概念漂移");
                    //更新分类器
                    this.Win.remove(pos);
                    this.Win.add(pos, data);
                    for(int i = 0; i < dataBlockLength; i++) {
                        this.labels.remove(pos*dataBlockLength);
                    }
                    this.labels.addAll(pos*dataBlockLength, Arrays.asList(this.svm.predict(data)));
                }

            }
        }
    }

    private void ExtendBlock(Vector<Double> topicProbability, int doc, Matrix block) {
        for(int i = 0; i < K ; i++) {
            int pos = 0;
            for(; pos < A.length; pos++) {
                if(A[pos] > topicProbability.getValue(i)) {
                    break;
                }
            }
            for (int j = 0; j < F[pos-1]; j++) {
                block.getValue(doc).add(topNword.get(i));
            }
        }
    }

    private void geneticPrioriParam(Vector<Integer> topic_biterms, Matrix<Integer> topic_word_count) {
        for (int k = 0; k < K; k++) {
            this.alphaInherited.setValue(k,this.alphaInherited.getValue(k)+Lambda*topic_biterms.getValue(k));
        }
        for(int k = 0; k < K; k++) {
            for (int v = 0; v < this.wordToIndex.size(); v++) {
                this.betaInherited.getValue(k).setValue(v,this.betaInherited.getValue(k).getValue(v)+Lambda*topic_word_count.getValue(k).getValue(v));
            }
        }
    }

    private double dist(Matrix<Double> matrix1, Matrix<Double> matrix2, List<Double> labels) {
        int allDoc = matrix2.getSize();
        /**
         * 划分类别簇
         */
        ArrayList<Matrix<Double>> cluster = divCluster(matrix2, labels);

        Double distances = .0;
        for(int i = 0; i < matrix1.getSize(); i++) {
            double distance = Double.MAX_VALUE;
            Param<Double> doc = (Param<Double>) matrix1.getValue(i);
            for(Matrix<Double> clu : cluster) {
                double dis = calculationDistance(doc, clu, allDoc);
                if(dis < distance) {
                    distance = dis;
                }
            }
            distances += distance;
        }
        return distances/matrix1.getSize();
    }

    private double calculationDistance(Param<Double> doc, Matrix<Double> cluster, int allDoc) {
        double distance = .0;
        for(int i = 0; i < cluster.getSize(); i++) {
            double a = .0;
            double b = .0;
            double c = .0;
            for(int j = 0; j < doc.getSize(); j++) {
                a += doc.getValue(j)*cluster.getValue(i).getValue(j);
                b += doc.getValue(j) * doc.getValue(j);
                c += cluster.getValue(i).getValue(j) * cluster.getValue(i).getValue(j);
            }
            distance += (1 - cluster.getSize()*1.0/allDoc*a/Math.sqrt(b)/Math.sqrt(c));
        }
        return distance/cluster.getSize();
    }

    private ArrayList<Matrix<Double>> divCluster(Matrix<Double> matrix, List<Double> labels) {
        ArrayList<Double> temp = new ArrayList<>();
        ArrayList<Matrix<Double>> result = new ArrayList<>();
        for(int i = 0; i < labels.size(); i++) {
            if(!temp.contains(labels.get(i))) {
                temp.add(labels.get(i));
                Matrix matrix1 = new Matrix();
                matrix1.add(matrix.getValue(i));
                result.add(matrix1);
            } else {
                result.get(temp.indexOf(labels.get(i))).add(matrix.getValue(i));
            }
        }
        return result;
    }

}
