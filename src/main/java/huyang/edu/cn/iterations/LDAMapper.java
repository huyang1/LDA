package huyang.edu.cn.iterations;

import huyang.edu.cn.LDA;
import huyang.edu.cn.Utils.MatrixOperations;
import huyang.edu.cn.data.MatrixKind;
import huyang.edu.cn.data.twoDimensionIndexWritable;
import huyang.edu.cn.vector.Matrix;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import huyang.edu.cn.sequence.Pair;
import huyang.edu.cn.sequence.SequenceFileIterable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LDAMapper extends Mapper<twoDimensionIndexWritable, Text, twoDimensionIndexWritable, Text> {
    private Map<String, Integer> wordToIndex = new HashMap<String, Integer>();

    private Matrix<Integer> docToTopic;

    private Matrix<Integer> topicToWord;

    private double alpha;

    private double beta;

    private int K;

    private int V;

    private int M;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        Configuration conf = context.getConfiguration();
        this.K = Integer.parseInt(conf.get("K"));
        this.alpha = Double.parseDouble(conf.get("alpha"));
        this.beta = Double.parseDouble(conf.get("beta"));
        ReadIndexFromFile(new Path(conf.get(LDA.indexFile)), conf);
        docToTopic = new Matrix<>(M, K, 0);
        ReadMatirxFromFile(new Path(conf.get(LDA.docToTopic)), conf, docToTopic, M,K);
        topicToWord = new Matrix<>(K, V, 0);
        ReadMatirxFromFile(new Path(conf.get(LDA.topicToWords)), conf, topicToWord, K,V);
    }

    @Override
    protected void map(twoDimensionIndexWritable key, Text value, Context context) throws IOException, InterruptedException {
        int m = key.getM();
        int oldTopic = key.getTopic();
        String word = value.toString();
        int index = wordToIndex.get(word);

        docToTopic.getValue(m).setValue(oldTopic,docToTopic.getValue(m).getValue(oldTopic)-1);
        topicToWord.getValue(oldTopic).setValue(index,topicToWord.getValue(oldTopic).getValue(index)-1);

        double[] p = new double[K];
        for(int k = 0; k < K; k++) {
            p[k] = (docToTopic.getValue(m).getValue(k)+alpha)/(MatrixOperations.sumByRow(docToTopic).getValue(m)+K*alpha)
                    *(topicToWord.getValue(k).getValue(index)+beta)/(MatrixOperations.sumByRow(topicToWord).getValue(k)+V*beta);
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

        docToTopic.getValue(m).setValue(newTopic,docToTopic.getValue(m).getValue(newTopic)+1);
        topicToWord.getValue(newTopic).setValue(index,topicToWord.getValue(newTopic).getValue(index)+1);

        key.setMatrixKind(MatrixKind.Corpus);
        key.setTopic(newTopic);

        context.write(key, value);

    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        for(int m = 0; m < M; m++) {
            for(int k = 0; k < K; k++) {
                twoDimensionIndexWritable param = new twoDimensionIndexWritable(m, k);
                param.setMatrixKind(MatrixKind.DocTopic);
                context.write(param, new Text(docToTopic.getValue(m).getValue(k).toString()));
            }
        }

        for(int k = 0; k < K; k++) {
            for(int v = 0; v < V; v++) {
                twoDimensionIndexWritable param = new twoDimensionIndexWritable(k, v);
                param.setMatrixKind(MatrixKind.TopicWord);
                context.write(param, new Text(topicToWord.getValue(k).getValue(v).toString()));
            }
        }
    }

    protected void ReadMatirxFromFile(Path path, Configuration conf, Matrix<Integer> matrix, int m, int n) {
        for (Pair<twoDimensionIndexWritable, IntWritable> record
                : new SequenceFileIterable<twoDimensionIndexWritable, IntWritable>(path, true, conf)) {
            twoDimensionIndexWritable key = record.getFirst();
            matrix.getValue(key.getM()).setValue(key.getN(), record.getSecond().get());
        }
    }

    protected void ReadIndexFromFile(Path path, Configuration conf) {
        boolean isM = true;
        for (Pair<Text, IntWritable> record
                : new SequenceFileIterable<Text, IntWritable>(path, true, conf)) {
            if(record.getFirst().equals(new Text())&&isM) {
                this.M = record.getSecond().get();
                isM = false;
            } else if(record.getFirst().equals(new Text())&&!isM) {
                this.V = record.getSecond().get();
            } else {
                wordToIndex.put(record.getFirst().toString(), record.getSecond().get());
            }
        }
    }

}
