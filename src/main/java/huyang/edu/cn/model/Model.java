package huyang.edu.cn.model;

import huyang.edu.cn.Utils.TwordsComparable;
import huyang.edu.cn.Utils.Util;
import huyang.edu.cn.vector.Matrix;
import huyang.edu.cn.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 顶层的model，接收用户设置的参数
 */
public abstract class Model {

    private static final Logger log = LoggerFactory.getLogger(Model.class);

    protected int K;

    protected int iterations ;

    protected int beginSave;

    protected int saveStepNum;

    protected String outputPath;

    protected int topN;

    protected Map<String, Integer> wordToIndex;

    protected Matrix<Integer> matrix;

    private long startTime = 0;

    public Model(int K, int iterations, int beginSave, int saveStepNum, String outputPath, int topN,
                 Map<String, Integer> wordToIndex,
                 Matrix<Integer> matrix) {
        this.K = K;
        this.iterations = iterations;
        this.beginSave = beginSave;
        this.saveStepNum = saveStepNum;
        this.outputPath = outputPath;
        this.topN = topN;
        this.wordToIndex = wordToIndex;
        this.matrix = matrix;
    }

    protected void initModel() {
        startTime = System.currentTimeMillis();
    }

    public void trainModel() {
        log.info("total cost time : {}",System.currentTimeMillis()-startTime);
        System.out.println("total cost time : "+ (System.currentTimeMillis()-startTime)+"ms");
    }

    abstract protected void estimatedParam();

    abstract protected int gibbs(int m, int n, Vector<Integer> doc);

    /**
     * 判断此次迭代是否满足阈值
     * @param A 迭代后矩阵
     * @param B 迭代前矩阵
     * @param threshold 阈值
     * @return
     */
    protected boolean isConvergence(Matrix<Double> A, Matrix<Double> B, double threshold) {
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

    /**
     *
     * @param iter 保存该参数所在的迭代次数
     * @param phi 在第i次迭代时，topic--words 矩阵
     * @throws Exception
     */
    protected void saveIteratedModel(int iter, Matrix<Double> phi) throws Exception{
        BufferedWriter writer;
        File file = new File(outputPath);
        if(file.exists()) {
            if(file.isDirectory()) {
                writer = new BufferedWriter(new FileWriter(outputPath+"/result-"+iter));
            } else {
                file.delete();
                writer = new BufferedWriter(new FileWriter(outputPath));
            }
        } else {
            file.createNewFile();
            writer = new BufferedWriter(new FileWriter(outputPath));
        }
        writer.write("Iterations is "+iter);
        writer.write("\n");
        Util util = new Util<String,Integer>();
        for(int k = 0; k < K; k++){
            List<Integer> tWordsIndexArray = new ArrayList<Integer>();
            for(int j = 0; j < wordToIndex.size(); j++){
                tWordsIndexArray.add(new Integer(j));
            }
            Collections.sort(tWordsIndexArray, new TwordsComparable(phi.getValue(k)));
            writer.write("topic " + k + "\t:\t");
            for(int t = 0; t < topN; t++){
                writer.write(util.getKey(wordToIndex,tWordsIndexArray.get(t))  + " " + phi.getValue(k).getValue(tWordsIndexArray.get(t)) + "\t");
            }
            writer.write("\n");
        }
        writer.close();
    }
}
