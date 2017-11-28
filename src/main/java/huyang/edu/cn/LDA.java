package huyang.edu.cn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LDA {

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
        BufferedReader br = new BufferedReader(new FileReader(path));
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

    public static void main(String[] args) throws Exception {
        matrix = new Matrix<Integer>();
        loadDocumentsFromFile("train.txt");
        Model model = new Model(wordToIndex,matrix);
        model.trainModel(matrix,0.00001);
    }

}
