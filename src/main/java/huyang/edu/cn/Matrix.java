package huyang.edu.cn;

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

    public Vector like() {
        return new Matrix();
    }

}
