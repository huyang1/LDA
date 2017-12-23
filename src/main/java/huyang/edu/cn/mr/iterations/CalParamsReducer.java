package huyang.edu.cn.mr.iterations;

import huyang.edu.cn.Job;
import huyang.edu.cn.Utils.TwordsComparable;
import huyang.edu.cn.Utils.Util;
import huyang.edu.cn.mr.data.MatrixKind;
import huyang.edu.cn.mr.data.indexToCountWritable;
import huyang.edu.cn.mr.data.twoDimensionIndexWritable;
import huyang.edu.cn.vector.Param;
import huyang.edu.cn.vector.Vector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import huyang.edu.cn.sequence.Pair;
import huyang.edu.cn.sequence.SequenceFileIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class CalParamsReducer extends Reducer<IntWritable, indexToCountWritable, twoDimensionIndexWritable, Text> {

    private static final Logger log = LoggerFactory.getLogger(CalParamsReducer.class);

    private Map<String, Integer> wordToIndex = new HashMap<String, Integer>();

    private double beta;

    private int V;

    private int M;

    private Vector<Double> phiByRow;//size = V

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        Configuration conf = context.getConfiguration();
        this.beta = Double.parseDouble(conf.get("params"));
        ReadIndexFromFile(new Path(conf.get(Job.indexFile)), conf);
        this.phiByRow = new Param<Double>(V,0.0);
    }

    @Override
    protected void reduce(IntWritable key, Iterable<indexToCountWritable> values, Context context) throws IOException, InterruptedException {
        int sum = 0;
        List<indexToCountWritable> data = new ArrayList<indexToCountWritable>();
        Iterator<indexToCountWritable> iter = values.iterator();
        while(iter.hasNext()) {
            indexToCountWritable temp = iter.next();
            data.add(temp);
            sum += temp.getCount();
        }

        for(indexToCountWritable indexToCount : data) {
            int n = indexToCount.getIndex();
            twoDimensionIndexWritable params = new twoDimensionIndexWritable(key.get(), n);
            double pro = (indexToCount.getCount() + beta) / (sum + V * beta);
            params.setMatrixKind(MatrixKind.TopicWord);
            params.setProbability(pro);
            Util util = new Util<String, Integer>();
            String word = (String) util.getKey(wordToIndex,n);
            phiByRow.setValue(n, pro);
            context.write(params, new Text(word));
        }

        List<Integer> tWordsIndexArray = new ArrayList<Integer>();
        for(int j = 0; j < V; j++){
            tWordsIndexArray.add(new Integer(j));
        }
        Util util = new Util<String,Integer>();
        Collections.sort(tWordsIndexArray, new TwordsComparable(phiByRow));
        log.info("topic " + key.get() + "\t:\t");
        for(int t = 0; t < 10; t++){
            log.info(util.getKey(wordToIndex,tWordsIndexArray.get(t))  + " " + phiByRow.getValue(tWordsIndexArray.get(t)) + "\t");
        }

    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {

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
