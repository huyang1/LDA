package huyang.edu.cn;

public interface Vector<T> {

    T getValue(int index);

    void setValue(int index, T value);

    int getSize();

    void add(T value);

    Vector like();

}