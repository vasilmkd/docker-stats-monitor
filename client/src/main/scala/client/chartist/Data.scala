package client.chartist

import scala.scalajs.js

class Data(initial: Double) extends js.Object {
  private var counter                    = 1
  val labels: js.Array[Int]              = js.Array(0)
  val series: js.Array[js.Array[Double]] = js.Array(js.Array(initial))

  def add(v: Double): Unit = {
    labels.push(counter)
    counter += 1
    series(0).push(v)
    ()
  }
}
