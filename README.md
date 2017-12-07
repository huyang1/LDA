HEAD  毕业设计：LDA算法实现与运用<br>
### 1.单机版实现：算法迭代主要是in-memory的方式，适合小数据量。<br>
### 2.MR实现：主要中间结果存在HDFS上.适合大数据集。<br>
#### 1>.
       第一个MR——————inputFile转化为wordIndexFile和M *N word file.初始化z topic word
#### 2>.
       第二个MR-----------在M*N word file 中初始化z，初始化doctopic，topicword文件（存于output/）。
#### 3>.
       在进行迭代时：1.MR------上个job的输出M*N word file（含z），topic，word 三个文件对M*N矩阵的word进行吉布斯采样。
       输出：更新topicword， doctopic，文件，新生成M*N word file 存于（output/iteration/iteration-？）路径供下次迭代使用。
#### 4>.
       saveModel，MR---只计算phi输出
###### 若有需要：对所有的saveModel的参数的平均值，使model更精确。（model存于output/model/model-？）<br>
###### （？代表当前迭代次数。）<br>
          
### 使用参数：
>>>>>>* -h    help                        print help message.<br>
>>>>>>* -i    inputFile                   train file input path.  default: the project's train.txt<br>
>>>>>>* -o    outputFile                  output Dir path. default: the project's result.txt<br>
>>>>>>* -k    K                            topic number,default: topic default 8<br>
>>>>>>* -b    beginSaveIterations      start save model params iterations<br>
>>>>>>* -s    saveStepNum                save model params num. <br>
>>>>>>* -it   maxIterations               LDA max iterations, <br>
>>>>>>* -mr   runMR                         if use MapReducer.<br>

