package huyang.edu.cn.Utils;

import huyang.edu.cn.vector.Matrix;
import huyang.edu.cn.vector.Param;
import huyang.edu.cn.vector.Vector;

/**
 * 本来这样函数应该写入Vector接口的，由于vecotr是泛型接口不好做加法，故只好单独写一个工具类实现
 */
public class MatrixOperations {
    /**
     * 为了避免内存使用过多，A将重复使用，没有新new 一个 Matrix
     * 所以：A是累加的结果，将不断累加，B做为累加数
     * @param A
     * @param B
     * @return
     */
    public static Matrix plus(Matrix<Double> A, Matrix<Double> B) {
        assert (A.getSize() == B.getSize());
        for(int i = 0; i < A.getSize(); i++) {
            assert (A.getValue(i).getSize() == B.getValue(i).getSize());
            for(int j = 0; j< A.getValue(i).getSize(); j++) {
                A.getValue(i).setValue(j,A.getValue(i).getValue(j)+B.getValue(i).getValue(j));
            }
        }
        return A;
    }

    public static Matrix div(Matrix<Double> A, double n) {
        for(int i = 0; i < A.getSize(); i++) {
            for(int j = 0; j< A.getValue(i).getSize(); j++) {
                A.getValue(i).setValue(j,A.getValue(i).getValue(j)/n);
            }
        }
        return A;
    }

    public static Vector<Integer> sumByRow(Matrix<Integer> A) {
        Vector<Integer> vector =new Param<Integer>();
        for(int i = 0; i < A.getSize(); i++) {
            int sum = 0;
            for(int j = 0; j< A.getValue(i).getSize(); j++){
                sum += A.getValue(i).getValue(j);
            }
            vector.add(sum);
        }
        return vector;
    }
}
