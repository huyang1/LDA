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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Driver {

    private static final  Logger log = LoggerFactory.getLogger(Driver.class);

    private int dataBlockLength;

    Map<String, Integer> wordToIndex;

    ArrayList<Matrix> blocks;

    private int topicN = 5;

    private int K = 8;

    private double Lambda = 0.5;

    private ArrayList<List<Integer>> topNword = new ArrayList<>();

    private double[] A = {0, 0.07, 0.1, 0.2, 0.5, 1};

    private int[] F = {0, 1, 2, 3, 4};

    private Vector<Double> alphaInherited;

    private Matrix<Double> betaInherited;

    public Driver(Map<String, Integer> wordToIndex, Matrix dataFlow, int dataBlockLength, int topicN) {
        this.wordToIndex = wordToIndex;
        this.dataBlockLength = dataBlockLength;
        this.topicN = topicN;
        this.blocks = dataFlow.Block(dataBlockLength);
        this.alphaInherited = new Param<Double>(K,.5);
        this.betaInherited = new Matrix<Double>(K, this.wordToIndex.size(), .001);
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
            Vector<Double> pro = btModel.predictionDoc(0);



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

}
