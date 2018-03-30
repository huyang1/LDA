package huyang.edu.cn.Utils;

import huyang.edu.cn.vector.Matrix;
import huyang.edu.cn.vector.Param;
import huyang.edu.cn.vector.Vector;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Util<K,V> {

    public  K getKey(Map<K,V> map,V value) {

        Set set = map.entrySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Map.Entry<K,V> entry = (Map.Entry) it.next();
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 概率抽样
     * @param p 概率密度数组
     * @return
     */
    public static int sample(double[] p) {

        int topic;
        for (int k = 1; k < p.length; ++k) {
            p[k] += p[k-1];
        }

        double u = Math.random() * p[p.length-1];
        for (topic = 0; topic < p.length; topic++) {
            if (u < p[topic])
                break;
        }
        return topic;
    }

//    /**
//     * 计算两个matrix之和
//     */
//    public static Matrix<Double> adds(Matrix<Double> matrix1, Matrix<Double> matrix2) {
//        assert (matrix1.getSize() == matrix2.getSize());
//        Matrix<Double> result = matrix1.like();
//        for(int i = 0; i < matrix1.getSize(); i++) {
//            result.add(add(matrix1.getValue(i),matrix2.getValue(i)));
//        }
//        return result;
//    }
//
//    /**
//     * 计算两个Param之和
//     */
//    public static Vector<Double> add(Vector<Double> param1, Vector<Double> param2) {
//        assert (param1.getSize() == param2.getSize());
//        Vector<Double> result = param1.like();
//        for(int i = 0; i < param1.getSize(); i++) {
//            result.add(param1.getValue(i)+param2.getValue(i));
//        }
//        return result;
//    }
//
//    /**
//     * 计算matrix的乘积
//     */
//    public static Matrix<Double> muls(Matrix<Double> matrix1, double x) {
//        Matrix<Double> result = matrix1.like();
//        for(int i = 0; i < matrix1.getSize(); i++) {
//            result.add(mul(matrix1.getValue(i),x));
//        }
//        return result;
//    }
//
//    /**
//     * 计算Param的乘积
//     */
//    public static Vector<Double> mul(Vector<Double> param1, double x) {
//        Vector<Double> result = param1.like();
//        for(int i = 0; i < param1.getSize(); i++) {
//            result.add(param1.getValue(i)*x);
//        }
//        return result;
//    }

}
