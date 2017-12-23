package huyang.edu.cn.mr.iterations;

import huyang.edu.cn.mr.data.indexToCountWritable;
import huyang.edu.cn.mr.data.twoDimensionIndexWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CalParamsMapper extends Mapper<twoDimensionIndexWritable, IntWritable, IntWritable, indexToCountWritable> {

    private static final Logger log = LoggerFactory.getLogger(CalParamsMapper.class);

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
    }

    @Override
    protected void map(twoDimensionIndexWritable key, IntWritable value, Context context) throws IOException, InterruptedException {
        int m = key.getM();
        int n = key.getN();
        context.write(new IntWritable(m), new indexToCountWritable(n, value.get()));
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {

    }
}
