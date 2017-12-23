package huyang.edu.cn.mr.iterations;

import huyang.edu.cn.Job;
import huyang.edu.cn.mr.data.indexToWordWritable;
import huyang.edu.cn.mr.data.twoDimensionIndexWritable;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InputReducer extends Reducer<LongWritable, indexToWordWritable, twoDimensionIndexWritable, Text> {
    private static final Logger log = LoggerFactory.getLogger(InputReducer.class);

    private Map<String, Integer> wordToIndex;

    private Path indexFilePath;

    private int m;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.wordToIndex = new HashMap<String, Integer>();
        this.m = 0;
        this.indexFilePath = new Path(context.getConfiguration().get(Job.indexFile));
    }

    @Override
    protected void reduce(LongWritable key, Iterable<indexToWordWritable> values, Context context) throws IOException, InterruptedException {
        if(key.get()<0) {
            for (indexToWordWritable value : values) {
                String word = value.getWord();
                if(!wordToIndex.containsKey(word)) {
                    wordToIndex.put(word, wordToIndex.size());
                }
            }
        } else {
            for(indexToWordWritable words : values) {
                context.write(new twoDimensionIndexWritable(m, words.getIndex()), new Text(words.getWord()));
            }
            log.info("m: {}",m);
            m++;
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        FileSystem fs = FileSystem.get(indexFilePath.toUri(),context.getConfiguration());
        SequenceFile.Writer writer = new SequenceFile.Writer(fs, context.getConfiguration(), indexFilePath, Text.class, IntWritable.class);
        for(String word : wordToIndex.keySet()) {
            writer.append(new Text(word), new IntWritable(wordToIndex.get(word)));
        }
        writer.append(new Text(), new IntWritable(m));
        writer.append(new Text(), new IntWritable(wordToIndex.size()));
        writer.close();
        super.cleanup(context);
    }

}
