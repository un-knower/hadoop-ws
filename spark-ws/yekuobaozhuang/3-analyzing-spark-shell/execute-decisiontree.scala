// ----------------------------------------------------------------------------
// --------------------------
// 聚类数据: (1) 只取参与计算的数据；(2) 转变为Vector, 构建matrices
import org.apache.spark.mllib.linalg.Vectors

// 取 平均值(年度月用电量)数据
val averageRdd = mappedRddData_percent.map(x => x.average).map(y => Vectors.dense(y.toArray)).cache()
// 取 平均值(年度月用电量)数据的百分比
val averageRdd_percent = mappedRddData_percent.map(x => x.averagePercent).map(y => Vectors.dense(y.toArray)).cache()

val dataForClustering = averageRdd_percent // averageRdd

val env = System.getenv()
val pwd = env.get("PWD")
// ----------------------------------------------------------------------------
// 任务2： 手工指定最佳K，计算模型，然后...）
// --------------------------
val perfectK = 50;
val maxIterations = 20 // 当前没有生效

val env = System.getenv()
val pwd = env.get("PWD")

// 聚类结果统计信息
val resultClusterCountInfo_Standalone = ComputeClusterCount_Standalone(dataForClustering, perfectK, maxIterations)
// 中心点和metric信息
val resultW2LocalFile_Standalone = writeAccount2LocalFile(resultClusterCountInfo_Standalone.account, "yekuobaozhuang", pwd)
// 统计信息
val resultWClusterCountInfo2HDFS_Standalone = writeClusterCountInfo2LocalFile(resultClusterCountInfo_Standalone, "yekuobaozhuang", pwd)

// ----------------------------------------------------------------------------
// 分类后的数据
// 类标签的数据
val predictRdd_Clustering = resultClusterCountInfo_Standalone.predict

// 元素数据和标签数据: 目前使用的是 百分比数据
// 变换顺序(原始值): rddFromHive(或从parquetFile) => mappedRddData => mappedRddData_percent => dataForClustering:averageRdd => predictRDD
// 变换顺序(百分比): rddFromHive(或从parquetFile) => mappedRddData => mappedRddData_percent => dataForClustering:averageRdd_percent => predictRDD

val first0 = rddFromHive.first
val first1 = mappedRddData.first
val first2 = mappedRddData_percent.first  // dataForClustering
val first3 = averageRdd_percent.first
val first4 = predictRdd_Clustering.first

rddFromHive // org.apache.spark.sql.SchemaRDD
mappedRddData // org.apache.spark.rdd.RDD[MPVolumeItem]
mappedRddData_percent // org.apache.spark.rdd.RDD[MPVolumeItem_AverageMonthVolume_percent] // dataForClustering
averageRdd_percent // org.apache.spark.rdd.RDD[org.apache.spark.mllib.linalg.Vector]
predictRdd_Clustering // org.apache.spark.rdd.RDD[Int]

first0
first1
first2
first3
first4

// ----------------------------------------------------------------------------
// 获得特征值数据

/*
// 不使用 sqlContext 中注册为表的形式,表中获得最后结果时,都增加了总括号!
// 获得各个属性信息
val attributesRDDLeft = mappedRddData_percent.map(x => x.index.left)
val attributesRDDRight = mappedRddData_percent.map(x => x.index.right)
//val attributeOfRunnedDays = mappedRddData_percent.map(x => x.runnedDays)
//// 获得各个属性信息的hashCode
//val attributesRDD_hashCode = mappedRddData_percent.map(x => ConvertIndex2IndexHashCode(x.index))
//val attributesRDDLeft = attributesRDD_hashCode.map(x => x.left)
//val attributesRDDRight = attributesRDD_hashCode.map(x => x.right)
////val attributeOfRunnedDays = mappedRddData_percent.map(x => x.runnedDays)

// sc is an existing SparkContext.
val sqlContext = new org.apache.spark.sql.SQLContext(sc)
// createSchemaRDD is used to implicitly convert an RDD to a SchemaRDD.
import sqlContext.createSchemaRDD
// 注册为表
attributesRDDLeft.registerTempTable("indexLeft")
attributesRDDRight.registerTempTable("indexRight")
attributesRDDRight.registerTempTable("runnedDays")

// SQL statements can be run by using the sql methods provided by sqlContext.
// ----------------------------------------------------------------------------
// left
// mp_id
// mp_no
// mp_name
val featureValue_mp_addr = sqlContext.sql("SELECT distinct mp_addr FROM indexLeft").collect() // 计量点地址/8968
val featureValue_volt_code = sqlContext.sql("SELECT distinct volt_code FROM indexLeft").collect() // 电压等级/3
// app_date
// run_date
val featureValue_org_no = sqlContext.sql("SELECT distinct org_no FROM indexLeft").collect() // 供电电位编号/23
val featureValue_mr_sect_no = sqlContext.sql("SELECT distinct mr_sect_no FROM indexLeft").collect() // 抄表段编号/2105
val featureValue_line_id = sqlContext.sql("SELECT distinct line_id FROM indexLeft").collect() // 线路标识/1863
val featureValue_tg_id = sqlContext.sql("SELECT distinct tg_id FROM indexLeft").collect() // 台区标识/14308
val featureValue_status_code = sqlContext.sql("SELECT distinct status_code FROM indexLeft").collect() // 用户状态:正常,当前新装,当前变更,已销户/4
val featureValue_cons_id = sqlContext.sql("SELECT distinct cons_id FROM indexLeft").collect() // 用户内部索引/12993
val featureValue_mp_cap = sqlContext.sql("SELECT distinct mp_cap FROM indexLeft").collect() // 计量点容量/350
val featureValue_cons_no = sqlContext.sql("SELECT distinct cons_no FROM indexLeft").collect() // 用户号/12993
val featureValue_zxzb = sqlContext.sql("SELECT distinct zxzb FROM indexLeft").collect() // 专线转变:1转变,2专线,/3
//cons_name
val featureValue_elec_addr = sqlContext.sql("SELECT distinct elec_addr FROM indexLeft").collect() // 用电客户的用电地址/8909
val featureValue_trade_code = sqlContext.sql("SELECT distinct trade_code FROM indexLeft").collect() // 行业代码/313
val featureValue_elec_type_code = sqlContext.sql("SELECT distinct elec_type_code FROM indexLeft").collect() // 营销系统中的用电类别/12

// ----------------------------------------------------------------------------
// right
val featureValue_elec_contract_cap = sqlContext.sql("SELECT distinct contract_cap FROM indexRight").collect() // 合同容量/347
val featureValue_run_cap = sqlContext.sql("SELECT distinct run_cap FROM indexRight").collect() // 运行容量/338
//val featureValue_build_date = sqlContext.sql("SELECT distinct build_date FROM indexRight").collect() // 安装日期/12790
val featureValue_ps_date = sqlContext.sql("SELECT distinct ps_date FROM indexRight").collect() // 首次送电日期/
//val featureValue_cancel_date = sqlContext.sql("SELECT distinct cancel_date FROM indexRight").collect() // 销户归档日期/
//val featureValue_due_date = sqlContext.sql("SELECT distinct due_date FROM indexRight").collect() // 临时用电客户约定的用电到期日期/1963
*/

// 获得各个属性信息
val attributesRdd = mappedRddData_percent
val categoricalFeaturesInfo_attributes = ComputeCategoricalFeaturesInfo(attributesRdd)
// ----------------------------------------------------------------------------
// 获得各个属性信息hashCode化,这个hash值就作为特征值
val attributesRdd_hashCode = mappedRddData_percent.map(x => (ConvertIndex2IndexHashCode(x.index), x.runnedDays))

// 将属性特征值转换为Vector
val featuresRdd = attributesRdd_hashCode.map(x => ConvertAttributesHashCode2Vector(x))

// 将分类标签和特征值Vector整合为 LabeledPoint
import org.apache.spark.mllib.regression.LabeledPoint

val zipRDD = featuresRdd.zip(predictRdd_Clustering)
val labeledPointRDD = zipRDD.map(x => new LabeledPoint(x._2, x._1)).cache()
// ----------------------------------------------------------------------------
// 执行决策树算法
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.util.MLUtils

// Load and parse the data file.
// Cache the data since we will use it again to compute training error.
// hdfs dfs -mkdir -p sample_libsvm_data.txt data/mllib/
// hdfs dfs -copyFromLocal -p sample_libsvm_data.txt data/mllib/sample_libsvm_data.txt
//val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt").cache()

val dataForDecisionTree = labeledPointRDD
/*
// ----------------------------------------------------------------------------
// 随机数模拟 dense LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

import scala.util.Random
import scala.math.abs
val randomer = new Random(100)

val range = Range(1, 11)
val randomData = for( i<- range) yield {
  val label = abs(randomer.nextInt(2).toDouble)
  val vector = Vectors.dense(Array(
    randomer.nextInt(10),
    randomer.nextInt(20),
    randomer.nextInt(100),
    randomer.nextInt(79),
    randomer.nextInt(110),
    randomer.nextInt(899)
  ).map(x => scala.math.abs(x).toDouble))
  new LabeledPoint(label, vector)
}
val data = sc.parallelize(randomData)
 */


// Train a DecisionTree model.
//  Empty categoricalFeaturesInfo indicates all features are continuous.
val numClasses = 50 //2
//val categoricalFeaturesInfo = Map[Int, Int]()
val categoricalFeaturesInfo_array = categoricalFeaturesInfo_attributes.map(x => {
  x.asInstanceOf[(Int, (Int, Int), Array[(String, Int)])]._2
})
val categoricalFeaturesInfo = categoricalFeaturesInfo_array.toMap
val impurity = "gini"
val maxDepth = 5
// val maxBins = 100
// java.lang.IllegalArgumentException: requirement failed: maxBins (= 100) should be greater than max categories in categorical features (>= 350)
val maxCategories = categoricalFeaturesInfo.values.reduce((a,b) => scala.math.max(a,b))
val maxBins = maxCategories + 1
// maxBins 太大导致 java.lang.OutOfMemoryError: Java heap space
// 调整 categoricalFeaturesInfo, 进而调整 maxBins

val model = DecisionTree.trainClassifier(dataForDecisionTree, numClasses, categoricalFeaturesInfo, impurity,
  maxDepth, maxBins)

// Evaluate model on training instances and compute training error
val labelAndPreds = dataForDecisionTree.map { point =>
  val prediction = model.predict(point.features)
  (point.label, prediction)
}
val trainErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / dataForDecisionTree.count
println("Training Error = " + trainErr)
println("Learned classification tree model:\n" + model)