package models

import scala.scalajs.js

trait StatsPlatformSpecific {
  def apply(obj: js.Dynamic): Stats = {
    val data = obj.selectDynamic("data").asInstanceOf[js.Array[js.Dynamic]].map(ContainerData(_)).toSet
    Stats(data)
  }
}
