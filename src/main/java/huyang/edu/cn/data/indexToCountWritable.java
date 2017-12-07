package huyang.edu.cn.data;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class indexToCountWritable extends Configured implements Writable {

    private int index;

    private int count;

    public indexToCountWritable() {
        this.index = Integer.MAX_VALUE;
    }

    public indexToCountWritable(int index) {
        this.index = index;
    }

    public indexToCountWritable(int index, int count) {
        this.index = index;
        this.count = count;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(index);
        dataOutput.writeInt(count);
    }

    public void readFields(DataInput dataInput) throws IOException {
        this.index = dataInput.readInt();
        this.count = dataInput.readInt();
    }
}

