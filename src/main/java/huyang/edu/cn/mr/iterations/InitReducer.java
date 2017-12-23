package huyang.edu.cn.mr.iterations;

import huyang.edu.cn.HadoopUtil;
import huyang.edu.cn.Job;
import huyang.edu.cn.mr.data.MatrixKind;
import huyang.edu.cn.mr.data.twoDimensionIndexWritable;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InitReducer extends Reducer<twoDimensionIndexWritable, Text, twoDimensionIndexWritable, Text> {
    private static final Logger log = LoggerFactory.getLogger(InitReducer.class);

    private Path docToTopicPath;

    private Path topicToWordsPath;

    private SequenceFile.Writer writer1;

    private SequenceFile.Writer writer2;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.docToTopicPath = new Path(context.getConfiguration().get(Job.docToTopic));
        this.topicToWordsPath = new Path(context.getConfiguration().get(Job.topicToWords));

        HadoopUtil.delete(context.getConfiguration(), docToTopicPath);
        HadoopUtil.delete(context.getConfiguration(), topicToWordsPath);

        this.docToTopicPath = new Path(context.getConfiguration().get(Job.docToTopic));
        this.topicToWordsPath = new Path(context.getConfiguration().get(Job.topicToWords));

        FileSystem fs = FileSystem.get(docToTopicPath.toUri(),context.getConfiguration());
        writer1 = new SequenceFile.Writer(fs, context.getConfiguration(), docToTopicPath, twoDimensionIndexWritable.class, IntWritable.class);
        fs = FileSystem.get(topicToWordsPath.toUri(),context.getConfiguration());
        writer2 = new SequenceFile.Writer(fs, context.getConfiguration(), topicToWordsPath, twoDimensionIndexWritable.class, IntWritable.class);
    }

    @Override
    protected void reduce(twoDimensionIndexWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        if(key.getMatrixKind().equals(MatrixKind.Corpus)) {
            context.write(key, values.iterator().next());
            return ;
        } else if(key.getMatrixKind().equals(MatrixKind.DocTopic)||
                key.getMatrixKind().equals(MatrixKind.TopicWord)){
            int count = 0;
            for(Text text : values) {
                count += Integer.parseInt(text.toString());
            }
            if (key.getMatrixKind().equals(MatrixKind.DocTopic)) {
                writer1.append(new twoDimensionIndexWritable(key.getM(), key.getN()), new IntWritable(count));
            } else {
                writer2.append(new twoDimensionIndexWritable(key.getM(), key.getN()), new IntWritable(count));
            }
        }
        return;

    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        writer1.close();
        writer2.close();
    }

}
