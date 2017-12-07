package huyang.edu.cn.mr;

import huyang.edu.cn.data.indexToCountWritable;
import huyang.edu.cn.data.twoDimensionIndexWritable;
import huyang.edu.cn.iterations.CalParamsMapper;
import huyang.edu.cn.iterations.CalParamsReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 只计算topidcToWord矩阵
 */
public class CalParamDriver {

    private static final Logger log = LoggerFactory.getLogger(CalParamDriver.class);

    public static void run(Configuration conf, Path inputPath, Path output, double params) throws IOException, ClassNotFoundException, InterruptedException {
        String jobName = "calculating parameter";
        conf.set("params",String.valueOf(params));

        Job job = new Job(conf, jobName);
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(indexToCountWritable.class);
        job.setOutputKeyClass(twoDimensionIndexWritable.class);
        job.setOutputValueClass(Text.class);

        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        job.setMapperClass(CalParamsMapper.class);
        job.setReducerClass(CalParamsReducer.class);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job,output);

        job.setJarByClass(LDADriver.class);
        if (!job.waitForCompletion(true)) {
            throw new InterruptedException("calculating parameter failed");
        }
    }
}
