// --------------------------------------------------------------------------
// 使用方法
//	在spark-shell中执行  (注意"1.加载数据"的语句即可)
//		:load  /home/hadoop/workspace_github/hadoop-ws/spark-ws/MLlib/userinfo-mining/execute-s01-v1.scala

// 1. 加载数据 
// (1)样本数据
:load  /home/hadoop/workspace_github/hadoop-ws/spark-ws/MLlib/userinfo-mining/create-parsedData-userinfo-s01-v2.scala
// (2)全量数据
//:load  /home/hadoop/workspace_github/hadoop-ws/spark-ws/MLlib/userinfo-mining/create-parsedData-userinfo-s98-v2.scala

// 2. 加载函数
// 聚类函数
:load  /home/hadoop/workspace_github/hadoop-ws/spark-ws/MLlib/userinfo-mining/tryKMeansSmart.scala
// 根据聚类模型分配clusterID
:load  /home/hadoop/workspace_github/hadoop-ws/spark-ws/MLlib/userinfo-mining/clusteringUserInfo-v2.scala

// ----------------------------------------------------------------------------
// 调试函数 ClusteringUserInfo_Standalone_Standalone
/*
val dataForModel = parsedData_GoodM2
val dataIndexed = parsedDataIndexed_GoodM2

val k = 4
val maxIterations = 20 // 当前没有生效
*/
// ----------------------------------------------------------------------------
// 任务1： 

val perfectK = 4;
val maxIterations = 20 // 当前没有生效

// 1. 有效数据
// (1) 单月数据 GoodM1
val clusteredInfo_GoodM1 = ClusteringUserInfo_Standalone(parsedData_GoodM1, parsedDataIndexed_GoodM1, perfectK, maxIterations, sc) 
val clusterSet_GoodM1 = ComputeClusterSet(clusteredInfo_GoodM1)
// (2) 双月数据 GoodM2
val clusteredInfo_GoodM2 = ClusteringUserInfo_Standalone(parsedData_GoodM2, parsedDataIndexed_GoodM2, perfectK, maxIterations, sc) 
val clusterSet_GoodM2 = ComputeClusterSet(clusteredInfo_GoodM2)
// --------------------------
// 2. 无效数据
// (1) 前三个月都是 0或NULL    BadF3
val clusteredInfo_BadF3 = ClusteringUserInfo_Standalone(parsedData_BadF3, parsedDataIndexed_BadF3, perfectK, maxIterations, sc) 
val clusterSet_BadF3 = ComputeClusterSet(clusteredInfo_BadF3)
// (2) 仅前两个月都是 0或NULL   BadF2ExcludeF3
val clusteredInfo_BadF2ExcludeF3 = ClusteringUserInfo_Standalone(parsedData_BadF2ExcludeF3, parsedDataIndexed_BadF2ExcludeF3, perfectK, maxIterations, sc) 
val clusterSet_BadF2ExcludeF3 = ComputeClusterSet(clusteredInfo_BadF2ExcludeF3)
// ----------------------------------------------------------------------------
// 交互式查询样本
val x = clusterSet_GoodM1
x.k
x.clusterCenters
x.clusterArray
getSampleFromClusterSet(x,0,10) // 从簇0中寻找10个样本
getSampleFromClusterSet(x,3,50) // 从簇0中寻找50个样本
getSampleFromClusterSet(x,x.k+1,2) // 从簇0中寻找2个样本  应该报错!
