## LDA in pyspark ##

**环境简述：**

1. centos7
2. python2.7 
3. jupyter notebook
4. VM上单机Spark2.2, 内存3G  CPU：2 core
5. VM上单机hadoop2.7
6. 宿主机：cpu：4 core 内存 12G ，反正电脑很渣

### 准备数据 ###

- 将RDD数据flatMap转化成dataFrame
- 初始化z docTopic topicWord V文件
- 进行gibbs采样
- 迭代步骤3 10次
- 计算phi矩阵，输出top-N 关键word

### 备注 ###

- **Z:** 存储所有文件的word，format：（index*topic）
- **V:** 所有不同word的index文件
- **docTopic:** M*K矩阵.代表文档m中topic：k下word的count
- **topicWord:** V*K矩阵.代表topic：k中word：v的count

### 优化问题 ###

inputFile：10060*40的word. 一行代表一个样本.</br>
不知道是不是自己优化没做好，还是硬件资源不够。导致程序在迭代进行gibbs采样时，程序龟速.</br>
迭代10次大概花费36h.</br>
希望有人帮我指导一下(^_^)

