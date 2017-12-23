package huyang.edu.cn.mr.driver;

import huyang.edu.cn.mr.data.indexToWordWritable;
import huyang.edu.cn.mr.data.twoDimensionIndexWritable;
import huyang.edu.cn.mr.iterations.InputMapper;
import huyang.edu.cn.mr.iterations.InputReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InputDriver {
    private static final Logger log = LoggerFactory.getLogger(InputDriver.class);

    public InputDriver() {}

    public static void runJob(Configuration conf, Path inputPath, Path output) throws IOException, ClassNotFoundException, InterruptedException {

        Job job = new Job(conf, "Input Drive running input:"+inputPath);
        log.info("start running InputDriver");
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(indexToWordWritable.class);
        job.setOutputKeyClass(twoDimensionIndexWritable.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(InputMapper.class);
        job.setReducerClass(InputReducer.class);
        job.setNumReduceTasks(1);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setJarByClass(InputDriver.class);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, output);

        boolean succeeded = job.waitForCompletion(true);
        if (!succeeded) {
            throw new IllegalStateException("Job failed!");
        }

    }

}
