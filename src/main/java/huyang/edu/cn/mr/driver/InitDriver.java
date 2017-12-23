package huyang.edu.cn.mr.driver;

import huyang.edu.cn.mr.data.twoDimensionIndexWritable;
import huyang.edu.cn.mr.iterations.InitMapper;
import huyang.edu.cn.mr.iterations.InitReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InitDriver {
    private static final Logger log = LoggerFactory.getLogger(InitDriver.class);

    public static void run(Configuration conf, Path[] inputPath, Path outputPath) throws IOException, ClassNotFoundException, InterruptedException {
            String jobName = "init matrix";
            Job job = new Job(conf, jobName);

            job.setMapOutputKeyClass(twoDimensionIndexWritable.class);
            job.setMapOutputValueClass(Text.class);
            job.setOutputKeyClass(twoDimensionIndexWritable.class);
            job.setOutputValueClass(Text.class);

            job.setInputFormatClass(SequenceFileInputFormat.class);
            job.setOutputFormatClass(SequenceFileOutputFormat.class);

            job.setMapperClass(InitMapper.class);
            job.setReducerClass(InitReducer.class);
            job.setNumReduceTasks(1);

            for(Path path : inputPath) {
                FileInputFormat.addInputPath(job, path);
            }
            Path output = new Path(outputPath, "initDir");
            FileOutputFormat.setOutputPath(job, output);

            job.setJarByClass(LDADriver.class);
            if (!job.waitForCompletion(true)) {
                throw new InterruptedException("Init failed");
            }
    }
}

