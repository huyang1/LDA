package huyang.edu.cn;

import huyang.edu.cn.job.AbstractJob;
import huyang.edu.cn.mr.InitDriver;
import huyang.edu.cn.mr.InputDriver;
import huyang.edu.cn.mr.LDADriver;
import huyang.edu.cn.vector.Matrix;
import huyang.edu.cn.vector.Param;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LDA extends AbstractJob{

    public static final String SPLIT = " ";

    public static final String indexFile = "indexFilePath";

    //M*K矩阵
    public static final String docToTopic = "docToTopic";

    public static final String topicToWords = "topicToWords";

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
        addOption("runMR","mr","if run map reducer.");
        Path inputPath;
        Path outputPath;
        int K;
        int beginSave;
        int saveNum;
        int iterations;
        boolean  runMR;
        Map<String,String> argMap = parseArguments(args);
        if (argMap == null) {
            return -1;
        } else if (argMap.size()==1 && argMap.containsKey("help")) {
            return 0;
        }
        if(argMap.containsKey("inputPath")) {
            inputPath = new Path(argMap.get("inputPath"));
        } else {
            inputPath = new Path("LdaInput");
        }
        if(argMap.containsKey("outputPath")) {
            outputPath = new Path(argMap.get("outputPath"));
        } else {
            outputPath = new Path("LdaOutput");
        }
        if(argMap.containsKey("topic")) {
            K = Integer.parseInt(argMap.get("topic"));
        } else {
            K = 8;
        }
        if(argMap.containsKey("beginSave")) {
            beginSave = Integer.parseInt(argMap.get("beginSave"));
        } else {
            beginSave = 90;
        }
        if(argMap.containsKey("saveNum")) {
            saveNum = Integer.parseInt(argMap.get("saveNum"));
        } else {
            saveNum = 10;
        }
        if(argMap.containsKey("Iterations")) {
            iterations = Integer.parseInt(argMap.get("Iterations"));
        } else {
            iterations = 100;
        }
        if(argMap.containsKey("runMR")) {
            runMR = Boolean.parseBoolean(argMap.get("runMR"));
        } else {
            runMR = true;
        }

        if(!runMR) {
            loadDocumentsFromFile(inputPath.toString());
            Model model = new Model(wordToIndex,matrix,outputPath.toString(),K,beginSave,saveNum,iterations);
            model.trainModel(matrix);
        } else {
            inputPath = new Path("LdaInput");
            outputPath = new Path("LdaOutput");
            Configuration conf = new Configuration();
            HadoopUtil.delete(conf, outputPath);
            Path indexFilePath = new Path(outputPath, indexFile);
            Path docToTopicPath = new Path(outputPath, docToTopic);
            Path topicToWordsPath = new Path(outputPath, topicToWords);
            conf.set(indexFile, indexFilePath.toString());
            conf.set(docToTopic, docToTopicPath.toString());
            conf.set(topicToWords, topicToWordsPath.toString());
            conf.set("K", String.valueOf(K));

            InputDriver.runJob(conf, inputPath, outputPath);

            FileSystem fs = FileSystem.get(outputPath.toUri(), conf);
            FileStatus[] inputFiles = fs.globStatus(new Path(outputPath,"*"), new PathFilter() {
                @Override
                public boolean accept(Path path) {
                    String name = path.getName();
                    return name.contains("part");
                }
            });//文件过滤

            Path[] iterationPaths = new Path[inputFiles.length];
            for(int index = 0; index < inputFiles.length; index++) {
                iterationPaths[index] = inputFiles[index].getPath();
            }

            conf.set(indexFile, indexFilePath.toString());
            conf.set(docToTopic, docToTopicPath.toString());
            conf.set(topicToWords, topicToWordsPath.toString());
            conf.set("K", String.valueOf(K));
            InitDriver.run(conf, iterationPaths,outputPath);

            Path initDir = new Path(outputPath, "initDir");
            fs = FileSystem.get(initDir.toUri(), conf);
            FileStatus[] wordsFile = fs.globStatus(new Path(initDir, "*"), new PathFilter() {
                @Override
                public boolean accept(Path path) {
                    String name = path.getName();
                    return name.contains("part");
                }
            });//文件过滤

            iterationPaths = new Path[wordsFile.length];
            for(int index = 0; index < wordsFile.length; index++) {
                iterationPaths[index] = wordsFile[index].getPath();
            }

            LDADriver.run(conf, iterationPaths, outputPath, iterations, beginSave, saveNum, indexFilePath, docToTopicPath, topicToWordsPath, K);
        }

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
