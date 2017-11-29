package huyang.edu.cn.Utils;

import huyang.edu.cn.vector.Vector;

import java.util.Comparator;

public class TwordsComparable implements Comparator<Integer> {

    public Vector<Double> sortProb; // Store probability of each word in topic k

    public TwordsComparable (Vector<Double> sortProb){
        this.sortProb = sortProb;
    }

    public int compare(Integer o1, Integer o2) {
        if(sortProb.getValue(o1) > sortProb.getValue(o2)) return -1;
        else if(sortProb.getValue(o1) < sortProb.getValue(o2)) return 1;
        else return 0;
    }
}
