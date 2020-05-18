package client.chartist

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

class Data(initial: Double) extends js.Object {
  val labels: js.Array[Int] = Array.range(0, 30).toJSArray
  val series: js.Array[js.Array[Double]] = {
    val array = new Array[Double](30)
    array(29) = initial
    js.Array(array.toJSArray)
  }

  def add(v: Double): Unit = {
    series(0).sliceInPlace(1, 30)
    series(0).push(v)
    ()
  }
}
