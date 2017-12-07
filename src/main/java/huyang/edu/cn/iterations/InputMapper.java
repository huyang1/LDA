package huyang.edu.cn.iterations;

import huyang.edu.cn.data.indexToWordWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class InputMapper extends Mapper<LongWritable, Text, LongWritable, indexToWordWritable>{

    private static final Pattern SPACE = Pattern.compile(" ");

    private Map<String, Integer> wordToIndex;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.wordToIndex = new HashMap<String, Integer>();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String[] words = SPACE.split(value.toString());
        int n = 0;
        for (String word : words) {
            context.write(key, new indexToWordWritable(n, word));
            if(!wordToIndex.containsKey(word)) {
                wordToIndex.put(word, wordToIndex.size());
            }
            n++;
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        int index = 1;
        for(String word : wordToIndex.keySet()) {
            Text value = new Text(word);
            context.write(new LongWritable(-1*index),new indexToWordWritable(index, value.toString()));
            index++;
        }
        this.wordToIndex.clear();
        super.cleanup(context);
    }
}
