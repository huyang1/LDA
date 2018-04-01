package huyang.edu.cn.DataFlowClassification;

import huyang.edu.cn.vector.Matrix;
import huyang.edu.cn.vector.Vector;

import libsvm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SVM {

    private static final Logger log = LoggerFactory.getLogger(SVM.class);

    private double[] labels;

    private svm_node[][] datas ;

    private int dataCount;

    private svm_model model;

    public SVM() {
        this.labels = new double[0];
        this.datas = new svm_node[0][0];
        this.dataCount = 0;
    }

    public SVM(ArrayList<Matrix<Double>> data, ArrayList<Double> labels) {
        this.labels = new double[labels.size()];
        for (int i = 0; i < labels.size(); i++) {
            this.labels[i] = labels.get(i);
        }

        this.datas = trans(data);

        this.dataCount = datas.length;

    }

    public void train() {
        svm_problem problem = new svm_problem();
        problem.l = dataCount; // 向量个数
        problem.x = datas; // 训练集向量表
        problem.y = labels; // 对应的lable数组

        // 定义svm_parameter对象
        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.EPSILON_SVR;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 100;
        param.eps = 0.00001;
        param.C = 1.9;


        System.out.println(svm.svm_check_parameter(problem, param));

        log.info(svm.svm_check_parameter(problem, param));

        // 如果参数没有问题，则svm.svm_check_parameter()函数返回null,否则返回error描述。
        this.model = svm.svm_train(problem, param);
    }

    public Double[] predict(Matrix<Double> x) {

        Double[] pre = new Double[x.getSize()];
        ArrayList<Matrix<Double>> matrix = new ArrayList<>();
        matrix.add(x);
        svm_node[][] data = trans(matrix);

        for(int doc = 0; doc < data.length; doc++) {
            pre[doc] = svm.svm_predict(this.model, data[doc]);
        }
        return  pre;
    }

    private svm_node[][] trans(ArrayList<Matrix<Double>> matrix) {
        svm_node[][] data;
        List<svm_node[]> nodeSet = new ArrayList<>();

        for (Matrix<Double> x : matrix) {
            for(int i = 0; i < x.getSize(); i++) {
                Vector<Double> doc = x.getValue(i);
                svm_node[] vector = new svm_node[doc.getSize()];
                for(int j = 0; j < doc.getSize(); j++) {
                    svm_node node = new svm_node();
                    node.index = j + 1;
                    node.value = doc.getValue(j);
                    vector[j] = node;
                }
                nodeSet.add(vector);
            }
        }

        data = new svm_node[nodeSet.size()][nodeSet.get(0).length];
        for(int i = 0; i < nodeSet.size(); i++) {
            for(int j = 0; j < nodeSet.get(i).length; j++) {
                data[i][j] = nodeSet.get(i)[j];
            }
        }
        return data;
    }



}
