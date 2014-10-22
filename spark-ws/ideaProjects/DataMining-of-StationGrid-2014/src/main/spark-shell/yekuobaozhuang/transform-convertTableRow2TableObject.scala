/**
 * 将数据表中的行 转换为 TableObject
 * Created by HongZe.Wu on 9/28/14.
 */

// 转换函数
// row --> VolumePriceItemLadder
def convertTableRow2TableObject(r:org.apache.spark.sql.Row):TableObject = {
  // 定义函数:对一个org.apache.spark.sql.Row中每一个列的处理函数
  // 函数原型为: g: (y: Any)Double
  def Any2Double(y:Any):Double = y match {
    case null => 0			// 将null转换为0
    case i:Int => i.toDouble	// 将Int转换为Double
    case d:Double => d
  }

  def any2TypeString(y:Any) = y match {
    case i:Int => i.toString
    case l:Long => l.toString
    case f:Float => f.toString
    case d:Double => d.toString
    case s:String => s.toString
    case Array(x, _*) => new java.lang.String(y.asInstanceOf[Array[Byte]], "utf-8")
    case other => other.toString
  }

  def testType(y:Any) = y match {
    case i:Int => print("Int")
    case l:Long => print("Long")
    case f:Float => print("Float")
    case d:Double => print("Double")
    case s:String => print("String")
    //case a:Array[Int] => print("Array")
    case Array(x,y,z,_*) => print("Array(x,y,z,_*)")
    case other => print("other")
  }

  // ------------------------------------
  val length = r.length
  val lenOfIndexInfo = 26	// 前26列是属性信息
  val rangeLeft = Range(0, lenOfIndexInfo)
  val rangeRight = Range(lenOfIndexInfo, length)

  val index = for(i <- rangeLeft) yield any2TypeString(r(i))  // scala.collection.immutable.IndexedSeq[Any]
  val value = for(j <- rangeRight) yield Any2Double(r(j)) // scala.collection.immutable.IndexedSeq[Double]

  // ------------------------------------
  val id = index(0)
  // 转换index 为 IndexInfo
  val indexLeft = IndexLeft(index(0), index(1), index(2), index(3), index(4), index(5), index(6),
    index(7), index(8), index(9), index(10), index(11), index(12), index(13), index(14), index(15),
    index(16), index(17), index(18), index(19))
  val indexRight = IndexRight(index(20), index(21), index(22), index(23), index(24), index(25))

  // ------------------------------------
  // 目前数据只有从 201201 到 201409 的, 所以YearsVolume中的y2010,y2011都为空;且y2014中10月份及其之后的为0
  val y2010:TwelveVolumes = null
  val y2011:TwelveVolumes = null
  val sliceNum = 12	// 一年12个月
  val y2012 = value.slice(0, sliceNum)
  val y2013 = value.slice(sliceNum, sliceNum*2)
  val y2014 = value.slice(sliceNum*2, sliceNum*3)

  val yearsVolumes = YearsVolumes(y2010,y2011,y2012,y2013,y2014)

  // ------------------------------------
  // 返回值
  val result = TableObject(Index(id, indexLeft, indexRight), yearsVolumes)
  return result
}