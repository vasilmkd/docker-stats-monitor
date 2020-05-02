package models

import scala.scalajs.js

trait ContainerDataPlatformSpecific {
  def apply(obj: js.Dynamic): ContainerData = {
    val id            = obj.selectDynamic("id").asInstanceOf[String]
    val name          = obj.selectDynamic("name").asInstanceOf[String]
    val cpuPercentage = obj.selectDynamic("cpuPercentage").asInstanceOf[Double]
    val memPercentage = obj.selectDynamic("memPercentage").asInstanceOf[Double]
    ContainerData(id, name, cpuPercentage, memPercentage)
  }
}
