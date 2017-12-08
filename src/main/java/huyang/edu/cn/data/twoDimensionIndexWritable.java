package huyang.edu.cn.data;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class twoDimensionIndexWritable extends Configured implements WritableComparable {

    protected int m;

    protected int n;

    private int topic=0;

    private double probability=0.0d;

    private String matrixKind="";

    /**
     * 特殊的key，用于判断是存储索引文件还是word文件
     */
    public twoDimensionIndexWritable() {
        m = Integer.MAX_VALUE;
        n = Integer.MAX_VALUE;
    }

    public twoDimensionIndexWritable(int m, int n) {
        this.m = m;
        this.n = n;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getTopic() {
        return topic;
    }

    public void setTopic(int topic) {
        this.topic = topic;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public String getMatrixKind() {
        return matrixKind;
    }

    public void setMatrixKind(String matrixKind) {
        this.matrixKind = matrixKind;
    }

    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(m);
        dataOutput.writeInt(n);
        dataOutput.writeInt(topic);
        dataOutput.writeDouble(probability);
        dataOutput.writeUTF(this.matrixKind);


    }

    public void readFields(DataInput dataInput) throws IOException {
        this.m = dataInput.readInt();
        this.n = dataInput.readInt();
        this.topic = dataInput.readInt();
        this.probability = dataInput.readDouble();
        this.matrixKind = dataInput.readUTF();
    }

    public int compareTo(Object o) {
        if(this.m > ((twoDimensionIndexWritable)o).getM()) {
            return 1;
        } else if(this.m == ((twoDimensionIndexWritable)o).getM()){
            if(this.n > ((twoDimensionIndexWritable)o).getN()) {
                return 1;
            } else if (this.n == ((twoDimensionIndexWritable)o).getN()) {
                if(this.matrixKind.equals(((twoDimensionIndexWritable) o).getMatrixKind())) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
}
