package huyang.edu.cn.vector;

import java.util.ArrayList;

public interface Vector<T> {

    T getValue(int index);

    void setValue(int index, T value);

    int getSize();

    void add(T value);

    Vector<T> like();
}
