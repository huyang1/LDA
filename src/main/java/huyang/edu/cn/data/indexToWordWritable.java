package huyang.edu.cn.data;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class indexToWordWritable extends Configured implements Writable{

    private int index;

    private String word;

    public indexToWordWritable() {
        this.index = Integer.MAX_VALUE;
    }

    public indexToWordWritable(int index) {
        this.index = index;
    }

    public indexToWordWritable(int index, String word) {
        this.index = index;
        this.word = word;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(index);
        dataOutput.writeUTF(word);
    }

    public void readFields(DataInput dataInput) throws IOException {
        this.index = dataInput.readInt();
        this.word = dataInput.readUTF();
    }
}
