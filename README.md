HEAD  毕业设计
LDA算法实现与运用
1.单机版实现：算法迭代主要是in-memory的方式，适合小数据量。
2.MR实现：主要中间结果存在HDFS上.适合大数据集。
          1>.第一个MR——————inputFile转化为wordIndexFile和M*N word file.
          2>.在进行迭代时：1.MR------上个job的输出z，topic，word 三个文件以及M*N矩阵的word进行吉布斯采样。
                           2.如果需要saveModel，1.MR---计算thera输出
                                                2.MR---计算phi输出
                           3.MR------求所有的saveModel的参数的平均值，使model更精确。

