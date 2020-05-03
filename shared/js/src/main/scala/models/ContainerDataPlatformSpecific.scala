package models

import scala.scalajs.js

private[models] trait ContainerDataPlatformSpecific {
  def apply(obj: js.Dynamic): ContainerData = {
    val id            = obj.selectDynamic("id").asInstanceOf[String]
    val name          = obj.selectDynamic("name").asInstanceOf[String]
    val cpuPercentage = obj.selectDynamic("cpuPercentage").asInstanceOf[Double]
    val memUsage      = obj.selectDynamic("memUsage").asInstanceOf[String]
    val memPercentage = obj.selectDynamic("memPercentage").asInstanceOf[Double]
    val netIO         = obj.selectDynamic("netIO").asInstanceOf[String]
    val blockIO       = obj.selectDynamic("blockIO").asInstanceOf[String]
    val pids          = obj.selectDynamic("pids").asInstanceOf[Int]
    ContainerData(id, name, cpuPercentage, memUsage, memPercentage, netIO, blockIO, pids)
  }
}
