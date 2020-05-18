package client.chartist

import scala.scalajs.js

trait Options extends js.Object {
  val low: js.Any
  val showArea: Boolean
  val showPoint: Boolean
  val fullWidth: Boolean
  val axisX: Axis
  val axisY: Axis
}

object Options {
  def apply(
    low: js.Any = js.undefined,
    showArea: Boolean = true,
    showPoint: Boolean = false,
    fullWidth: Boolean = true,
    axisX: Axis = Axis(),
    axisY: Axis = Axis(true, true)
  ): Options =
    js.Dynamic
      .literal(
        "low"       -> low,
        "showArea"  -> showArea,
        "showPoint" -> showPoint,
        "fullWidth" -> fullWidth,
        "axisX"     -> axisX,
        "axisY"     -> axisY
      )
      .asInstanceOf[Options]
}
