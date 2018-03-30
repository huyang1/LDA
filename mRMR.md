## 基于高维的大数据快速最小冗余最大相关算法 ##


### 序言 ###
随着数据大规模问题的出现,对于输入降维而言特征选择已经成为基本预处理步骤。mRMR选择器凭借高准确率被认为是降低维度的最相关方法之一，然而它又是一个计算代价昂贵的技术。维数显著影响算法效率。本文提出mRMR算法扩展---快速mRMR算法，试图克服计算负担这一不足。与fast-mRMR算法相关的是：我们在多平台构建了一个包含该算法三种实现的封装包。

- CPU用于程序顺序执行
- GPU用于程序并行执行
- spark平台利用大数据技术实现分布式计算


### 介绍 ###

在过去的几年中，数据集在很多领域的维度如生物信息学或文本分析方面已经严重增加。 这个事实在研究界引出了一个有趣的挑战，因为许多机器学习（ML）方法不能有效地处理大量的输入特征。 事实上，如果我们分析流行的libSVM数据库中发布的数据集，我们可以发现观察到数据的最大维度已增2900多万。同样，其中一些算法在遇到大样本时也会受到影响。由于现在通常处理的数据集在特征数量和样本数量方面都太大，所以在这个新的情况下现有的机器学习方法都需要改变。</br>

为了解决这个问题，可以应用降维技术减少特征数量并提高后续学习的性能
处理。 最广泛使用的降维策略之一是特征选择（FS），通过消除不相干来实现降维和冗余功能.由于FS保持了原有的功能，因此尤其是对模型解释和知识提取的应用十分重要。 但是，现有的FS方法不能很好地弹性处理大规模的问题（无论是在特征和实例数量方面），事实上，他们的效率可能会显着恶化，甚至可能会变得不适用。</br>

在广泛的可用FS方法中，mRMR过滤器方法已经成为过去几年中使用最为频繁的一种。 参考文献五报告其大受欢迎（由于其准确性），尽管它是一个计算
昂贵的方法。 该方法使用特征的数量进行二次比例缩放相对于样本大小线性增长。 另一方面，mRMR由于其在计算中不包括条件冗余而饱受诟病。 然而，在参考文献 6，作者给出良好的表现，然而对于很多问题而言这个词显然是无用的。</br>

在本文中，我们介绍快速mRMR，包括mRMR的扩展:一些效率优化能够解决大维度问题。这些优化基于一组技术（如缓存相关信息或新的数据访问式），目的是提高mRMR的总体性能。 据我们所知，这是本文的第一步：试图解决mRMR的弱点。 除了这个重新设计，我们提供一个包含三个基于不同平台的快速mRMR版本的软件包：用于小型数据集的C ++的顺序版本，GPU-CUDA（计算统一设备体系结构）并行版本以及使用Apache Spark的大数据分布式版本。 他们都执行上述优化，但基于不同平台也有一些重要的变化（例如，在大数据分布式平台中广播的使用）。</br>

通过在不同平台和实际数据集上进行全面的实验（输入实例和功能<=107）用以评估这个改进版本性能。 随后的结果显示在所有测试的平台上我们的优化对于mRMR的性能有显著的提高。</br>

本文的其余部分组织如下：在第2节中，我们主要描述与这项工作有关的概念，并介绍mRMR方法及其在处理几个现实世界的问题上的重要性。 第3节介绍了优化目的列表，以及我们的软件包中算法的不同实现。 第4节解释实验结果通过比较对标准mRMR版本提出建议。 最后，第5节总结了这篇论文。
### 2.预步骤 ###

本节介绍与我们目标相关的主要概念和相关软件包，如FS，大数据和GPU处理等等。 我们也出席mRMR并强调现实世界中大多数问题因此技术受益。

##### 2.1. 特征选择 #####
FS可以定义为检测相关特征和丢弃无关和1冗余特征的过程，其目标是对于给定的问题用最小的性能下降参数表示，从而获得该功能的子集。 它有几个优点，比如提高机器学习算法的性能或让使用更简单的模型以提高速度成为可能。</br>
形式上，一个标准的FS问题可以定义如下：让Er成为一个实例Er =（E1r，...，Enr，Ecr），其中Eir对应于第r个样本的第i个特征值,并且Ecr表示该样本r的输出类别值为c。 让我们假设一个训练数据集D有m个样本，样本Er包含n个特征的集合X，测试集DT。 让我们定义S⊆X。S为通过FS算法生成的选择特征的子集。S中的这些特征被认为是与输入特征实体最为相关的。</br>
特征选择方法可以根据两种不同的方法进行划分：个体评估和子集评估。 个人评价是也被称为特征排名，并通过根据他们的相关度分配评估各个特征权重。 另一方面，子集评估根据一定的搜索策略产生候选特征子集。 每个候选子集通过一定的评估指标进行评估并进行比较，选择指标最好的一个候选子集。
而个人评价由于冗余特性很可能无法删除冗余功能。对于相似的排名，子集评价方法能够处理具有特征相关性的特征冗余。然而在子集生成步骤中，此框架中的方法可能会因为需要搜索所有特征子集而导致的不可避免的问题。</br>

除了这种分类之外，在基于FS算法与归纳学习之间的关系上，还有三种主流方法用于推断模型9。</br>
1. 过滤器，它依赖于训练数据的一般性。并在激励算法的预处理步骤执行FS。该模型具有计算量小、泛化能力强等优点。
2. 包装器，包含使用学习算法的一个黑盒，使用其预测性能来评估变量子集的相对有用性。换句话说，FS算法使用学习方法作为一个计算子程序即需要调用学习算法来评估每个子集的功能。然而，与过滤器相比，这种与分类交互的方法倾向于提供更好的性能结果。
3. 嵌入式方法，在训练过程中执行FS算法。通常是特定于学习机器的特定方法。因此，在分类器构造中搜索特征的最优子集，可以看作是在特征子集和假设的组合空间中的一种搜索。相比于包装器，这种方法能够以较低的计算成本获得所需依赖项。</br>
在这项工作中，我们将专注于一种称为最大最小冗余的相关性（MRMR），它是属于一种过滤方法即返回的所有功能的一个有序的分类排序。关于这个方法的更多细节在下一小节中给出。
##### 2.2. mRMR：规划与应用 #####
mRMR方法（首先由彭等人开发的。文献4）在ML社区中是最强大的过滤器（如高引用计数所示）。一开始，这种方法主要是用来处理DNA微阵列数据,由于极大量的特征和少量的样本使得这成为一个具有挑战性的ML研究领域。作者阐述基因（特征选择）通过mRMR算法对DNA表型特征数据在更大范围的空间帧上提供一个更加平衡的采集范围。目前，该方法已应用在其它领域，例如对正在冷却的风扇进行异常检测。眼运动分析，性别分类，多光谱卫星图像分析，有时在将mRMR算法运用在预处理步骤中会出现一些高维问题。如：文本图像分析。</br>

mRMR算法的可扩展性并不能被忽视，相反，是最重要的。在文献5中，展示了可扩展性的mRMR算法分析，以及由于其精确的报道被频繁使用于几个特定领域。但是在计算方面也存在一些高复杂度问题。其复杂度与特征数量呈二次相关，与样本数量线性相关。mRMR算法也因在选择的过程中未考虑到类条件冗余而为人所诟病。然而布朗等人在实验中表明许多问题是毫无意义的。mRMR算法在稳定度与精确度之间提供了一个最好的权衡。
mRMR算法主要用于在给定的分类问题上为其特征集合的重要性排序。该方法主要是对特征基于目标相关度进行排序，同时对冗余的特征进行惩罚。主要目标是在特征集合X与类别集合C中要找到一组最大依赖关系。用互信息（MI）（由i表示）。在方程1中定义了一对特征之间的MI。一旦边缘概率p（a）和p（b），以及这两个特征之间的联合概率p（a，b）已知。就可以有效地得到MI。</br>
方程1：</br>
在高维空间中实现最大依赖准则不是一个容易解决的任务.也就是说，由于样本的数量往往是不够的，而且，评估多元密度通常意味着昂贵的计算。另一种方法是用于确定最大关联准则。最大关联准则搜索满足以下方程的特征：</br>
方程2：</br>
根据最大关联准则选择特征可能会带来大量冗余。因此，如参考文献4所示，必须增加以下最小冗余度标准：</br>
方程3：</br>
mRMR算法准则由准则D与准则R组合优化来表示。在实践中，可以采用贪心算法，其中s是所选特征集：</br>
方程4：</br>
对于选择原始版本的1.0 mRMR伪代码做为算法1.在该算法中，主要的瓶颈与计算两个元素之间的MI有关。要么是给定的与类的特征（第4行），要么是一对输入特征（第7行）。这个函数是在每个特征之间计算的。尽管许多无关的特征对的最终结果对高维问题是无效的。</br>
算法1：mRMR原始版本</br>
输入：候选集和想要的特征数</br>
//候选集即初始特征集。</br>
//想要的特征数即需要选择的特征个数</br>
输出：选择出来的特征集。</br>
//。。。。。。伪代码。。。。。。。//</br>
    
    for feature fi in candidates do
    relevance = mutualInfo(fi, class);
    redundancy = 0;
    for feature fj in candidates do
    redundancy += mutualInfo(fi, fj);
    end for
    mrmrValues[fi] = relevance - redundancy;
    end for
    selectedFeatures = sort(mrmrValues).take(numFeaturesWanted);
</br>在文献中这一标准已被证明是最相关的。它的优化将是以下部分的主要重点，因为如果直接执行，mRMR算法将会变得低效，并且其可扩展性可能会受到损害。
##### 2.3. 大数据下的高维特征选择 #####
从大量的数据集合中提取有价值的信息已经成为数据分析研究中最重要和最复杂的挑战之一，这个概念就是我们常说的大数据。许多知识提取算法在大数据的环境下已经过时。因此，我们需要新的方法，能够有效地管理如此大量的数据，并确保性能不能下降。Gartner在文章中通过大数据概念介绍了3V模型：高数据量，高速，数据信息多样性使得我们需要一个新的能够应对大数据规模的处理过程。之后，在此基础上又增加了高准确性与高价值的概念。</br>

从一开始，数据科学家一般只关注大数据的一个方面，在早期，它即指大量的实例；对特征方面的关注较少。这种现象，也称为“大维度”，产生这种情况主要是由于数以千计甚至百万计特征数据的出现导致特征量激增。然而，大维度环境下要求新的FS策略和方法，能够处理这种现象产生的组合效应。很多的公开数据集仓库（如UCI与LibSVM）中大多数新的数据集已经在计算智能方面反应出这种问题。</br>

现在特征的数量以及特征的组合变换多样性已经成为很多应用程序的标准。由于并不是所有的特征都对结果存在相关性。因此FS算法应用于分类与预测过程中以产生模型高速高性价比的过程。现在从原始的输入特性（潜在的不相关、冗余和噪声）中选择显著的特征数量比以往任何时候都要多。同时还需满足特征规模与内部存储等条件要求，是大数据研究中最重要的挑战之一。</br>

近年来，在大数据环境下出现了许多大规模数据处理平台。Apache下的Spakr是这种环境中最强大的引擎之一。这个平台是一种将用户程序数据加载到内存中并反复查询，使之成为在线迭代处理（特别是ML算法）的合适工具。在Spark中，驱动程序（主程序）负责控制多个从节点（奴隶）并从他们那里收集状态信息，而从节点从分布式文件系统读取数据块（分区），执行一些计算并保存结果至本地分区。</br>

Spark是基于分布式的数据结构也被称为弹性分布式数据集（RDDS）。程序自动将任务分解成RDD分区。保证本地数据的持久性。	除此之外。RDD也是一个多用途工具，允许开发者将结果立刻持久化的保存在内存或磁盘中以达到可重用的目的，并且能够自定义数据分区以优化数据存储。
##### 2.4. GPU的发展 #####
GPU一般都用于图形渲染，但是他们对于并行计算（如机器学习与并行计算）的能力是最近被发现进行探究。在现有的许多应用程序如：物理模拟，信号处理，金融建模，神经网络，以及无数其他领域上，并行算法运行在GPU上往往能达到同类算法在CPU上速率的100倍。因此，它是mRMR算法运行的最理想环境。特别是当数据大小超过CPU版本的处理能力时。</br>

CUDA是NVIDIA7公司在GPU上实现的编程模型和并行计算平台。CUDA提供了直接访问GPU并行存储器的虚拟指令集。在CUDA中，计算分布在一个网格状的线程块上，所有的块包含相同数量的线程（参见图1）。内核是一种特殊的程序，用于在多个线程上并行执行，这些线程按块分组。只有同一块的线程可以直接通信并进行同步。</br>

内核可以利用寄存器、共享和全局内存d等机制来实现线程间计算的通信。前两个级别可以很快访问，但其存储量很小。如果程序过多的使用这些资源。将会导致其他进程由于缺少内存资源而挂起。另一方面，全局变量并不用于实际情况，因为其访问速度非常慢，因此内核需仔细平衡以下因素：1.共享内存和寄存器的使用。2.活动处理单元。3.全局内存访问入口数以及其访问模式。一个典型的CUDA程序包括以下几个阶段：</br>
1. 分配GPU全局内存区域
2. 运行GPU内核时，将数据从随机存取存储器（RAM）移动到全局存储器。
3. 将结果从全局内存返回到RAM，并释放GPU全局内存。
</br>
对于mRMR算法而言，这就意味着我们必须要将每个特征数据从RAM移入GPU全局内存。然后运行算法内核，在mRMR算法主循坏中将结果移回RAM。
### 3.FAST-MRMR:mRMR算法在高维方面的延伸 ###
在本节中，我们描述了mRMR算法的延伸。以及与该方法有关的优化列表。此外，介绍了一个实现了不同功能的fast-mRMR算法的软件包。详细的介绍了该软件包的并行版本与分布式版本。因为对原始版本算法的修改主要集中于此。
##### 3.1. fast-mRMR 优化 #####
在mRMR算法的原始版本中，主要的瓶颈是如何计算两个元素之间的MI值：要不给定一个特定的类要么给定一组输入特征对。为了解决这个问题，我们为mRMR提出了一个贪婪算法，如算法2中描述一样。该算法通过特征数来从原始特征集中选择特征子集以减少特征之间的比较数。将mRMR视为一种排序方法，该算法的目的是当归约特征数（输入特征）时，该贪婪算法不会影响最终结果。以这种方式，原始算法的复杂性将会被转化一个线性顺序的迭代过程。但会被迭代次数（选择的特征数）影响。</br>

fast-mRMR首先为所有的特征计算其相关值，然后缓存数据为下面的步骤进行复用。根据相关值选择最佳特征集作为冗余度的参考，然后根据mRMR算法循坏选择剩余的特征。对于每个选择的特征，计算该特征与所有未被选择的特征之间的MI值。在每次迭代时，将结果值缓存并累计至与该特征相关的变量中。最后，根据mRMR算法选择最优的特征存入最终特征集中。当特征选择循坏结束时，将选择的最终特征集作为新的特征集。
    
    1: INPUT: candidates, numFeaturesWanted
    2: // candidates is the set of initial features
    3: // numFeaturesWanted is the number of selected features
    4: OUTPUT: selectedFeatures // The set of selected features.
    5: selectedFeatures = ();
    6: for feature f in candidates do
    7: relevancesVector[f] = mutualInfo(f, class);
    8: acumulatedRedundancy[f] = 0; //Begin with no redundancy.
    9: end for
    10: selected = getMaxRelevance(relevancesVector);
    11: lastFeatureSelected = selected
    12: selectedFeatures.add(selected);
    13: candidates.remove(selected);
    14: while selectedFeatures.size() <numFeaturesWanted do
    15: for feature fc in candidates do
    16: relevance = relevancesVector[fc]
    17: acumulatedRedundancy[fc] += mutualInfo(fc, lastFeatureSelected);
    18: redundancy = acumulatedRedundancy[fc] / selectedFeatures.size();
    19: mrmr = relevance - redundancy;
    20: if mrmr is maximun then
    21: fc = lastFeatureSelected;
    22: end if
    23: end for
    24: selectedFeatures.add(lastFeatureSelected);
    25: candidates.remove(lastFeatureSelected);
    26: end while

此外，我们还应用了一系列优化设计，以减轻这些关键操作的复杂性。这些优化将在下面的列表：

1. 冗余计算：计算每对特征之间的MI值可能是非常复杂的。一种可能的优化是在每次迭代中累积冗余值。因此只计算最后选择的特征值与未被选择的特征值集之间的MI值。正如前面提到的，我们使用贪婪算法来解决这个问题（见等式4）。
2. 缓存边缘计算值：为了避免在每次迭代中重复计算边缘概率，故在程序开始时只计算一次边缘概率，并在以后的迭代中使用缓存。
3. 数据访问模式：mRMR算法的数据访问模式被认为是智能选择。相比于其他的ML算法而言：其主要的访问模式为行访问模式。（见图2a）虽然这是一个技术上细微差别，但由于随机访问比智能访问需要更大的成本，故使其性能得到显著的提高。这对GPU是特别重要的。其中GPU中的所有数据都必须从RAM转入其全局内存。这里，数据存储在内存中的存储方式为柱状格式（如图2b所示）。





图2：示例有三个样本和五个特征，每个特征由不同的颜色表示。</br>

为了确保原始算法不包括上述的优化，我们从作者的webPage上深入了解mRMR算法的源代码。请注意，上述优化只影响性能，并没有改变原来的最终的结果。因此，我们的方法可以被视为一个对原始版本优化的选择。</br>

有关fast-mRMR算法，我们提供了一个包含不同算法实现的软件包。并且为CPU设计了一个C++的串行版本，在GPU上实现CUPA的并行版本以支持ML算法。并且在apache Spark上用scala设计了一个分布式版本。顺序版本是在C++上直接实现了上述优化列表。下面的部分将详细描述其他版本，因为其他版本是对原始版本进行了一些特殊的扩展。
##### 3.2. fast-mRMR 在Apache Spark的分布式版本 #####
在这一节，主要解释了程序用于适应fast-mRMR算法的分布式范式。其中大多数代码已经适应于该范式。除了主循环保持不变，新版本已经包含以下几步:

- 柱形变换：这种变换的背后思想是对原始数据的每个分区的本地数据矩阵进行转置。进而将新结果数据以这种新格式缓存至内存。并在下面步骤中重用（数据的本地性）。在接下来，每个分区块中产生新的矩阵，格式为每个特征为一行，而不是每个实例一行。为了把所有的块中相同的特征放在一起，算法将数据分组将数据按组存储在相同的分区集合中。该方法保存了原始数据的数据模式与数据的本地性。图3利用一个具有两个分区与四个特征的小例子解释了上述过程。</br>
图3：柱形模式转换，F代表特征，I表示实例。左边的每个矩形代表原始数据集中的一个寄存器。右边的每个矩形代表一个新的柱形格式的转置特征块。
图4：直方图的创建模式，F表示特征和I表示实例。每个较轻的矩形代表柱形数据模式中的单个特征块。较深的矩形代表次要变量副本的分布。

- 相关性与冗余性：一旦数据是柱状模式，该算法需计算所有候选特征的直方图（参见图4）。这些柱状图统计了每个候选特征与次级特征（类值或者最后被选择的特征）之间出现的次数。根据算法，此操作以本地的方式执行，并保持数据局部性。以便每个特性具有独立计算其相关性或冗余性所需的所有信息。

- 互信息：这个阶段的目的是计算用于排序候选特征的MI值。作为一个起始步骤，该算法先在集群中备份次要变量的边缘比例和值。然后在每个最终特征上启动一个单独的进程。保留了数据的本地性。并上每个特征上进行隔离执行。程序首先计算每个特征的边缘比例。以及其与次级变量的联合概率。然后，对每个组合的结果所需要的全部信息进行计算和汇总,得到关于每个特征最终的MI值。（见公式1）图5给出了该操作示例。


##### 3.3. fast-mRMR 程序在GPU上并行版本 #####
为了把fast-mRMR应用于GPU上。我们采用了基于先验知识和边缘概率计算MI的混合方法。这种混合策略遵循以下逻辑：

- 如果可能输出结果的数量低于64，目前的GPU可以不使用全局内存使用的处理单元的全套制作。因此，在算法3的内核使用。目前的GPU可以使用全部的处理单元而不需要不使用全局内存。该内核在算法3中被应用。
- 如果可能的输出结果数大于64，小于256，则算法3中陈述的内核将不再有效，因为缺少全局共享内存而导致处理单元的使用率急剧下降。在这种情况下，该内核被使用与算法4.
- 如果可能的结果数量超过256，那么共享内存本身就不能作为选择，因此计算应该进行分区计算。该策略在算法5中详细说明。

###### 3.3.1 线程内核 ######
这个内核是为那些输出结果少于64个值的情况而设计的。考虑到当前内存的限制，每个线程有足够的内存来设置本地特征计算（最大性能）。在这种情况下，没有任何内存访问冲突，因为每个特征是独立计算的。注意，在随后的内核中有一个慢原子操作称为原子add钟需要解决这些冲突，而在该算法中是一个简单的加法。在这种情况下，每个线程不必担心序列化存储写入，因为在这方面其实现了显著的加速。</br>

    1: INPUT: data //The vector with the feature values.
    2: OUTPUT: histogram // A vector containing the count for each possible value.
    3: //Initialize one localHistogram per thread to zeros.
    4: i = threadId;
    5: while i < data.size() do
    6: localhistogram[data[threadId]]++;
    7: i += totalActiveThreads;
    8: end while
    9: reduceLocalHistograms(histogram) //partial histogram are merged in histogram.
    10: return histogram;

###### 3.3.2 变形内核 ######

这个内核是被设计用来计算多达256个可能值的特征，主要用于计算其边缘概率（见算法4）。CUDA中的每个流多处理器提供48 KB的内存（SMX），还有每个SMX最多64进程（一个32个线程）。这意味着，
如果所有的进程正在运行，每个进程有768字节的可用共享内存。由于进程768个字节不足以容纳有256个值的特征，所以处理单元的一个小百分比（比线程内核小得多）应该是空闲状态。每个进程都有它的局部特征。共享内存将通过相应的位置执行线性传递。一旦计算出局部特征，它们最终合并到全局内存中，并送回RAM。
    
    1: INPUT: data //The vector with the feature values.
    2: OUTPUT: histogram // A vector containing the count for each possible value.
    3: //Initialize one sharedHistogram per warp to zeros.
    4: i = threadId;
    5: while i < data.size() do
    6: atomicAdd(sharedHistogram[data[threadId]], 1);
    7: i += totalActiveThreads;
    8: end while
    9: reduceSharedHistograms(histogram) //partial histogram are merged in histogram.
    10: return histogram;


###### 3.3.3 混合内核 ######
最后一个内核被设计用来计算联合概率，允许计算最多65536个特征的特殊情况。因为在这种情况下，共享内存完全不够用，计算被分区以平衡访问数据集的次数和共享内存的使用。每个横轴限制768个值。内核需要访存3次数据集以完全计算1756种可能情况。在真实情况中，为了获取计算时间性能，可以保持进程空闲或每次访存数据计算更多的特征值。虽然这种方式每次访存都很慢，但最终的执行时间可能会更快，因为其不需要进行数据同步，该内核在主循坏上增加一个循环计数器，以便在每次迭代过程中获取当前计算特征的信息。

    Algorithm 5 Joint Kernel Main Loop
    1: INPUT: data //The vector with the values from two features.
    2: OUTPUT: histogram // A vector containing the count for each possible combination.
    3: totalBins = getTotalBins(data);
    4: for i = 0; i <totalBins / maxBinsPerStep; i++ do
    5: kernelThird(data, lap, globalHistogram)
    6: end for
</br>

    Algorithm 6 Joint Kernel
    1: INPUT: data, lap, globalHistogram
    2: // data is The vector with the feature values.
    3: // globalHistogram is the vector where shared histograms will be merged.
    4: OUTPUT: histogram // A vector containing the count for each possible value.
    5: if lap = 0 then
    6: Initialize globalHistogram to zeros.
    7: end if
    8: Initialize sharedHistogram per warp to zeros.
    9: i = threadId;
    10: while i < data.size() do
    11: bin = data[threadId]];
    12: if bin ≥ lap * maxBinsPerStep and bin <(lap + 1) * maxBinsPerStep then
    13: atomicAdd(sharedHistogram[bin], 1);
    14: end if
    15: i += totalActiveThreads;
    16: end while
    17: reduceSharedHistograms(globalHistogram) //partial histogram are merged.
    18: return globalHistogram;

### 4.实验分析 ###
这一部分是对基于不同内核实现fast-mRMR算法的性能评估。首先我们提出了一个用于评估不同fast-mRMR的实验框架。这些实验所得的结果也在下面进行了分析。
##### 4.1.实验框架 #####
顺序实验：标准电脑硬件配置：Intel Core i5-4690S（6M高速缓存，高达3.9GHZ）4 GB RAM DDR3 DIMM, 和 1 TB HDD (7200 rpm S-ATA。参考文献4中提到的所有数据集都已考虑进实验比较中。</br>

为了比较算法CPU与GPU的运行版本，生成了一组具有不同采样率的合成数据集。这些值随机生成在0, 30范围内，并且服从均匀分布，虽然其服从均匀分布，但是生成数据主要是为了提高fast-mRMR算法的执行效率。算法性能并不会受其影响。最后，利用一个NVIDIA GeForce GTX 780m装置作为GPU执行参考。</br>

最后，实现了spark分布式版本的算法是使用三个大型数据集进行评估，第一个数据集（以下称为ecbdl14）在gecco-2014国际会议上被提及。为了解决该不平衡问题，文献22中的随机过采样（ROS）算法的MapReduce版本被提出。其他数据集（ε和kddb）都是LIBSVM库的一部分。在该实验中，集群由一个主节点和18个计算节点组成。计算节点配置如下：两个英特尔至强CPU e5-2620，六核处理器，2 GHz，15mb缓存，QDR InfiniBand网络（40 Gbps），2 TB的硬盘，64 GB的RAM。
##### 4.2.结果分析 #####
表一和图6描述了在选择时间上fast-mRMR和顺序版本mRMR算法之间的比较。结果表明，fast-mRMR在所有情况下优于原来的版本，实现最大加速（MRMR时间/快速选择时间）值387.83。</br>

表二，是fast-mRMR算法在GPU和CPU的版本上进行比较。如本表所示，GPU版本对具有大量实例的数据集获得更好的结果，而CPU版本对于较小的实例则稍好一些。</br>

分布式版本的性能在表三中进行了评估,结果展示了spark版本是如何实现良好的加速比（即：CPU时间/saprk运行时间）证明计算负担在集群上均匀分布。需要注意的是，对于ecbdl14，其CPU时间用一个子集进行估计，然而对于kddb，这是不可能的，因为其数据集格式是稀疏的，并不能兼容原始版本。注意，该实验并没有评估fast-mRMR的分类性能，因为其与原始版本选择的数据集是一样。</br>

另一项实验展示了该算法对不同数量数据的执行性能，在这种情况下，主要改变选择过程中使用的内核数目。这里的ecbdl14数据集作为参考，并且与先前的研究使用相同的参数。图7描述了我们的算法在选择100个特性时所花费的时间，并用不同的内核数从10到100。图7显示了随着核心数目增加的对数曲线。注意，对于10个内核，内存不足，无法将整个数据集保存在内存中，这将影响算法的性能。</br>

根据前面的结果，我们可以得出关于上述版本的差异和使用的一些结论。如表二所示，在实例数量足够大的情况下使用并行实现（GPU）是合适的（大约100, 000左右）。在这种情况下，CPU版本开始运行比GPU版本慢，并行执行成为更好的选择。影响上述版本性能的主要因素是要处理的功能的数量。在表三中，我们可以观察到CPU版本不好处理具有大量特征（≥2000左右）的任务，在这些情况下，spark版本是最好的选择。类似地，当实例数量变大时（大约20000000），需要分布式执行。最后，对于存在数百万特征的稀疏问题，需要再次使用spark版本。请注意，上述优化版本的分类性能评估并没有进行，因为该版本与原始算法生成相同的结果（对于特征选择）。
### 5.结语 ###
在本文中，我们提出了一个所谓的fast-mRMR算法扩展。这个扩展包括几个优化，避免掉不必要的计算和优化原始算法所保留的数据访问模式。通过这些优化，对MRMR原来的二次复杂性已转化为一个高效的贪婪的过程。同样，MI计算的性能也得到了极大的改善。我们还提供了一个包实现三种fast-mRMR：C++的顺序版本，GPU的并行与分布式版本，Apache spark、版本。最后两个版本通过每个平台的原生原语对算法进行了全面的重新设计。</br>

实验结果表明，在某些情况下在我们的优化下mRMR原始算法性能能够提高374倍。在大数据集（O（107）实例和特征）下的附加实验中使用并行与分布式版本更加展示出明显的改进。此外，上述结果证明了在当前对mRMR优化扩展的必要性，因为真实世界的数据集的大小总是不断增加。对于大数据来说，这一点尤其重要，因为我们的方法可以用经典方法解决不可能解决的问题。
##### 致谢 #####
这项工作是由西班牙国家研究项目tin2013-47210-p，tin2014-57251-p，支持和tin-2015-65069-c2-1-r，和安达卢西亚研究计划p11-tic—7765、p12-tic-2958，由xunta de加利西亚自治区通过研究项目GRC 2014 / 035（所有项目由欧盟部分资助联邦基金）。美国的RAM´ı苏亚雷斯加列戈持有西班牙教育与科学部FPU奖学金（fpu13 / 00047）d.沃尔玛´ı牛鼻雷戈和V. Bolon Canedo承认的支持下´xunta de加利西亚自治区博士后基金代码pos-a / 2013/196和2014 / 164-0 ed481b。
##### 引用 #####

    1. Zhai Y, Ong Y, Tsang IW. The emerging “big dimensionality”. IEEE Comput Intell Mag2014;9(3):14–26.
    2. Chang C-C, Lin C-J. LIBSVM: a library for support vector machines. ACMTrans Intell Syst Technol 2011;2:27:1–27:27. Datasets available at http://www.csie.ntu.edu.tw/cjlin/libsvmtools/datasets/.
    3. Liu H, Motoda H. Feature selection for knowledge discovery and data mining. Norwell,MA: Kluwer; 1998.
    4. Peng H, Long F, Ding C. Feature selection based on mutual information criteria of maxdependency, max-relevance, and min-redundancy. IEEE Trans Pattern Anal Mach Intell2005;27(8):1226–1238.
    5. Rego-Fernandez D, Bol ´ on-Canedo V, Alonso-Betanzos A. Scalability analysis of mRMR ´for microarray data. In: Proc 6th Int Conf on Agents and Artificial Intelligence, March 6–8,2014, in ESEO, Angers, Loire Valley, France. pp 380–386.
    6. Brown G, Pocock A, Zhao M, Lujan M. Conditional likelihood maximisation: a unifying framework for information theoretic feature selection. J Mach Learn Res 2012;13:27–66.
    7. NVIDIA-CUDA. Programming guide. http://docs.nvidia.com/cuda/index.html. Accessed April 2016.
    8. Apache Spark: Lightning-fast cluster computing. Apache Spark; 2015. http://shop.oreilly.com/product/0636920028512.do. Accessed April 2016.
    9. Guyon I. Feature extraction: foundations and applications. Studies in Fuzziness and Soft Computing, Vol. 207. Berlin: Springer; 2006.
    10. Yu L, Liu H. Efficient feature selection via analysis of relevance and redundancy. J MachLearn Res 2004;5:1205–1224.
    11. Ding C, Peng H. Minimum redundancy feature selection from microarray gene expression data. J Bioinform Comput Biol 2005;3(2):185–205.
    12. Jin X, Ma E, Chang L, Pecht M. Health monitoring of cooling fans based on Mahalanobis distance with mRMR feature selection. IEEE Trans Instrum Meas 2012;61(8):2222–2229.
    13. Bulling A, Ward J, Gellersen H, Troster G. Eye movement analysis for activity recognition using electrooculography. IEEE Trans Pattern Anal Mach Intell 2011;33(4):741–753. International Journal of Intelligent Systems DOI 10.1002/int152 RAM´ IREZ-GALLEGO ET AL.
    14. Tapia J, Perez C. Gender classification based on fusion of different spatial scale features selected by mutual information from histogram of lbp, intensity, and shape. IEEE Trans Inform Forensics Secur 2013;8(3):488–499.
    15. Bratasanu D, Nedelcu I, Datcu M. Interactive spectral band discovery for exploratory visual analysis of satellite images. IEEE J Sel Top Appl Earth Obs Remote Sens 2012;5(1):207–224.
    16. Hu Y, Milios EE, Blustein J. Interactive feature selection for document clustering. In: Proc 2011 ACM Symp on Applied Computing (SAC ’11), March 21–24, 2011, in Tunghai University, Taichung, Taiwan. pp 1143–1150.
    17. Guo D. Coordinating computational and visual approaches for interactive feature selectionand multivariate clustering. Inform Visual 2003;2(4):232–246.
    18. Laney D. 3D data management: controlling data volume, velocity and variety; 2001. http://blogs.gartner.com/doug-laney/files/2012/01/ad949-3D-Data-ManagementControlling-Data-Volume-Velocity-and-Variety.pdf. Accessed April 2016.
    19. Lichman M. UCI machine learning repository; 2013.
    20. Kruger J, Westermann R. Linear algebra operators for GPU implementation of numerical ¨algorithms. ACM Trans on Graph 2003;22:908–916.
    21. Catanzaro B, Sundaram N, Keutzer K. Fast support vector machine training and classification on graphics processors. In: Proc 25th Int Conf on Machine Learning (ICML ’08), July 5–9,2008, in Helsinki, Finland. pp 104–111.
    22. del R´ ıo S, Lopez V, Ben ´ ´ ıtez JM, Herrera F. On the use of MapReduce for imbalanced big data using Random Forest. Inform Sci 2014;285(285):112–137.
