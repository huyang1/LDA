package huyang.edu.cn.vector;

import java.util.ArrayList;

public class Param< T > implements Vector<T> {

    private ArrayList<T> data;

    public Param() {
        data = new ArrayList<T>();
    }

    public Param(ArrayList<T> data) {
        this.data = data;
    }

    public Param(int size, T value) {
        this.data = new ArrayList<T>();
        for(int i=0; i<size; i++) {
            this.add(value);
        }
    }

    public boolean equal(Param<T> x) {
        if(this.getSize() != x.getSize()) {
            return false;
        } else {
            for(int i =  0; i < this.getSize(); i++) {
                if(!x.getData().contains(getValue(i))) {
                    return false;
                }
            }
            return true;
        }
    }


    public T getValue(int index) {
        return this.data.get(index);
    }

    public void setValue(int index, T value) {
        this.data.set(index,value);
    }

    public int getSize() {
        return this.data.size();
    }

    public void add(T value) {
        this.data.add(value);
    }

    private ArrayList<T> getData() {
        return this.data;
    }

    public Param like() {
        Param param = new Param(new ArrayList<T>());
        return param;
    }


}
