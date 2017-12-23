package huyang.edu.cn.mr.driver;

import huyang.edu.cn.Job;
import huyang.edu.cn.mr.data.twoDimensionIndexWritable;
import huyang.edu.cn.mr.iterations.InitReducer;
import huyang.edu.cn.mr.iterations.LDAMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LDADriver {
    private static final Logger log = LoggerFactory.getLogger(LDADriver.class);

    public static void run(Configuration conf, Path[] inputPath, Path outputPath, int maxIterations, int beginSave, int saveNum, Path indexFilePath,
                            Path docToTopicPath, Path topicToWordsPath, int K) throws IOException, ClassNotFoundException, InterruptedException {
        int iteration = 0;
        while(iteration < maxIterations) {
            String jobName = "Gibbs sample running iteration:"+iteration;

            conf.set(Job.indexFile, indexFilePath.toString());
            conf.set(Job.docToTopic, docToTopicPath.toString());
            conf.set(Job.topicToWords, topicToWordsPath.toString());
            conf.set("K", String.valueOf(K));
            conf.set("alpha", String.valueOf(50d/K));
            conf.set("beta", String.valueOf(0.01));

            org.apache.hadoop.mapreduce.Job job = new org.apache.hadoop.mapreduce.Job(conf, jobName);
            job.setMapOutputKeyClass(twoDimensionIndexWritable.class);
            job.setMapOutputValueClass(Text.class);
            job.setOutputKeyClass(twoDimensionIndexWritable.class);
            job.setOutputValueClass(Text.class);

            job.setInputFormatClass(SequenceFileInputFormat.class);
            job.setOutputFormatClass(SequenceFileOutputFormat.class);

            job.setMapperClass(LDAMapper.class);
            job.setReducerClass(InitReducer.class);

            for(Path path : inputPath) {
                FileInputFormat.addInputPath(job, path);
            }

            Path temp = new Path(outputPath,"iteration");
            Path tempMatrix = new Path(temp, "iteration-"+iteration);
            FileOutputFormat.setOutputPath(job, tempMatrix);
            job.setJarByClass(LDADriver.class);
            if (!job.waitForCompletion(true)) {
                throw new InterruptedException("Cluster Iteration " + iteration + " failed processing. ");
            }

            //计算matrix参数。
            if(iteration >= beginSave) {
                Path model = new Path(outputPath,"model");
                Path currentModel = new Path(model,"model-"+iteration);
                CalParamDriver.run(conf, topicToWordsPath, currentModel ,0.01);
            }

            FileSystem fs = FileSystem.get(tempMatrix.toUri(), conf);
            FileStatus[] wordsFile = fs.globStatus(new Path(tempMatrix, "*"), new PathFilter() {
                @Override
                public boolean accept(Path path) {
                    String name = path.getName();
                    return name.contains("part");
                }
            });//文件过滤
            Path[] iterationPaths = new Path[wordsFile.length];
            for(int index = 0; index < wordsFile.length; index++) {
                iterationPaths[index] = wordsFile[index].getPath();
            }
            inputPath = iterationPaths;

            //对保存的model求平均值
            iteration++;
        }
    }

}
