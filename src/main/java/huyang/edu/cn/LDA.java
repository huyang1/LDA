package huyang.edu.cn;

import huyang.edu.cn.job.AbstractJob;
import huyang.edu.cn.vector.Matrix;
import huyang.edu.cn.vector.Param;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LDA extends AbstractJob{

    public static final String SPLIT = " ";

    /**
     * word--index 向量
     */
    private static Map<String, Integer> wordToIndex = new HashMap<String, Integer>();

    /**
     * M*N矩阵,存储word的index
     */
    private static Matrix matrix;

    public LDA() {
        this.matrix = new Matrix<Integer>();
    }

    public static void loadDocumentsFromFile(String path) throws IOException {
        File file;
        if((file=new File(path)).isDirectory()) {
           for(String subFile : file.list()) {
               loadDocumentsFromFile(subFile);
           }
        } else {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            int m=0;
            while((line=br.readLine())!=null) {
                String[] words = line.split(SPLIT);
                matrix.add(new Param<Integer>());
                for(String word : words) {
                    if(!wordToIndex.containsKey(word)) {
                        wordToIndex.put(word, wordToIndex.size());
                    }
                    int index = wordToIndex.get(word);
                    matrix.getValue(m).add(index);
                }
                m++;
            }
        }
    }

    public static int run(String[] args) throws Exception {
        addOption("inputFile","i","train file input path.   default: the project's train.txt");
        addOption("outputFile","o"," output Dir path. default: the project's result.txt");
        addOption("K","k","topic number,default: " + "topic default 8");
        addOption("beginSaveIterations","b","start save model params iterations");
        addOption("saveStepNum","s"," save model params num. tip: beginSaveIterations+saveStepNum must < maxIterations");
        addOption("maxIterations","it","LDA max iterations, default is 500");
        String inputPath;
        String outputPath;
        int K;
        int beginSave;
        int saveNum;
        int iterations;
        Map<String,String> argMap = parseArguments(args);
        if (argMap == null) {
            return -1;
        } else if (argMap.size()==1 && argMap.containsKey("help")) {
            return 0;
        }
        if(argMap.containsKey("inputPath")) {
            inputPath = argMap.get("inputPath");
        } else {
            inputPath = "train.txt";
        }
        if(argMap.containsKey("outputPath")) {
            outputPath = argMap.get("outputPath");
        } else {
            outputPath = "result.txt";
        }
        if(argMap.containsKey("topic")) {
            K = Integer.parseInt(argMap.get("topic"));
        } else {
            K = 8;
        }
        if(argMap.containsKey("beginSave")) {
            beginSave = Integer.parseInt(argMap.get("beginSave"));
        } else {
            beginSave = 490;
        }
        if(argMap.containsKey("saveNum")) {
            saveNum = Integer.parseInt(argMap.get("saveNum"));
        } else {
            saveNum = 10;
        }
        if(argMap.containsKey("Iterations")) {
            iterations = Integer.parseInt(argMap.get("Iterations"));
        } else {
            iterations = 500;
        }
        loadDocumentsFromFile(inputPath);
        Model model = new Model(wordToIndex,matrix,outputPath,K,beginSave,saveNum,iterations);
        model.trainModel(matrix);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        matrix = new Matrix<Integer>();
        if(args.length>0) {
            run(args);
        } else {
            loadDocumentsFromFile("train.txt");
            Model model = new Model(wordToIndex,matrix);
            model.trainModel(matrix);
        }
    }

}
