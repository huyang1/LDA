package huyang.edu.cn.mr.iterations;

import huyang.edu.cn.Job;
import huyang.edu.cn.mr.data.MatrixKind;
import huyang.edu.cn.mr.data.twoDimensionIndexWritable;
import huyang.edu.cn.mr.sequence.*;
import huyang.edu.cn.vector.Matrix;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InitMapper extends Mapper<twoDimensionIndexWritable, Text, twoDimensionIndexWritable, Text>{

    private static final Logger log = LoggerFactory.getLogger(InitMapper.class);

    private Map<String, Integer> wordToIndex = new HashMap<String, Integer>();

    private Matrix<Integer> docToTopic;

    private Matrix<Integer> topicToWord;

    private int K;

    private int M;

    private int V;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        Configuration conf = context.getConfiguration();
        this.K = Integer.parseInt(conf.get("K"));
        ReadIndexFromFile(new Path(conf.get(Job.indexFile)), conf);
        docToTopic = new Matrix<Integer>(M, K, 0);
        topicToWord = new Matrix<Integer>(K, V, 0);
    }

    @Override
    protected void map(twoDimensionIndexWritable key, Text value, Context context) throws IOException, InterruptedException {
        int m = key.getM();
        int n = key.getN();
        int index = wordToIndex.get(value.toString());
        int initTopic = (int)(Math.random()*K);
        twoDimensionIndexWritable keys = new twoDimensionIndexWritable(m,n);
        keys.setTopic(initTopic);
        keys.setMatrixKind(MatrixKind.Corpus);
        context.write( keys, value);
        docToTopic.getValue(m).setValue(initTopic,docToTopic.getValue(m).getValue(initTopic)+1);
        topicToWord.getValue(initTopic).setValue(index,topicToWord.getValue(initTopic).getValue(index)+1);

    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        for(int m = 0; m < M; m++) {
            for(int k = 0; k < K; k++) {
                twoDimensionIndexWritable param = new twoDimensionIndexWritable(m, k);
                param.setMatrixKind(MatrixKind.DocTopic);
                context.write( param, new Text(docToTopic.getValue(m).getValue(k).toString()));
            }
        }

        for(int k = 0; k < K; k++) {
            for(int v = 0; v < V; v++) {
                twoDimensionIndexWritable param = new twoDimensionIndexWritable(k, v);
                param.setMatrixKind(MatrixKind.TopicWord);
                context.write( param, new Text(topicToWord.getValue(k).getValue(v).toString()));
            }
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
