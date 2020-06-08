package client.chartist

import scala.scalajs.js

trait Axis extends js.Object {
  val showLabel: Boolean
  val showGrid: Boolean
}

object Axis {
  def apply(showLabel: Boolean = true, showGrid: Boolean = true): Axis =
    js.Dynamic.literal("showLabel" -> showLabel, "showGrid" -> showGrid).asInstanceOf[Axis]
}
