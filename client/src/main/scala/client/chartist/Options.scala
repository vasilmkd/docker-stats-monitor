package client.chartist

import scala.scalajs.js

trait Options extends js.Object {
  val low: js.Any
  val high: js.Any
  val showArea: Boolean
}

object Options {
  def apply(
    low: js.Any = js.undefined,
    high: js.Any = js.undefined,
    showArea: Boolean = true,
    showPoint: Boolean = false,
    fullWidth: Boolean = true,
    axisX: Axis = Axis(),
    axisY: Axis = Axis()
  ): Options =
    js.Dynamic
      .literal(
        "low"       -> low,
        "high"      -> high,
        "showArea"  -> showArea,
        "showPoint" -> showPoint,
        "fullWidth" -> fullWidth,
        "axisX"     -> axisX,
        "axisY"     -> axisY
      )
      .asInstanceOf[Options]
}
