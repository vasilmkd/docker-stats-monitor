package client.chartist

import scala.scalajs.js

trait Axis extends js.Object {
  val showLabel: Boolean
  val showGrid: Boolean
}

object Axis {
  def apply(showLabel: Boolean = false, showGrid: Boolean = false): Axis =
    js.Dynamic.literal("showLabel" -> showLabel, "showGrid" -> showGrid).asInstanceOf[Axis]
}
