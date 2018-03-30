package huyang.edu.cn.vector;

import java.util.ArrayList;

public class Matrix<T> implements Vector<Vector<T>> {

    private Vector<Vector<T>> matrix;

    public Matrix(int row, int col, T value) {
        this.matrix = new Param<Vector<T>>();
        for(int i=0; i<row; i++) {
            Param<T> oneRow = new Param<T>();
            for(int j=0; j<col; j++) {
                oneRow.add(value);
            }
            this.matrix.add(oneRow);
        }
    }

    public Matrix() {
        this.matrix = new Param<Vector<T>>();
    }

    public Vector<T> getValue(int index) {
        return this.matrix.getValue(index);
    }

    public void setValue(int index, Vector<T> value) {
        this.matrix.setValue(index, value);
    }

    public int getSize() {
        return this.matrix.getSize();
    }

    public void add(Vector<T> value) {
        this.matrix.add(value);
    }

    public void add(int m, T value) {
        this.matrix.getValue(m).add(value);
    }

    public Matrix like() {
        return new Matrix();
    }

    /**
     * 数据集分块
     * @param length ： 块大小
     * @return
     */
    public ArrayList<Vector> Block(int length) {
        ArrayList<Vector> blocks = new ArrayList<Vector>();
        for(int i = 0; i < getSize()/length; i++) {
            Vector block = like();
            for(int j = i*length; j < length*(i+1); j++) {
                block.add(getValue(j));
            }
            blocks.add(block);
        }
        if(getSize()%length !=0) {
            Vector block = like();
            for(int i = length*(getSize()/length); i < getSize(); i++) {
                block.add(getValue(i));
            }
            blocks.add(block);
        }
        return blocks;
    }

    public int count(Param<T> x) {
        int count = 0;
        for(int i = 0; i < this.getSize(); i++) {
            if(((Param) this.getValue(i)).equals(x)) {
                count++;
            }
        }
        return count;
    }

}
