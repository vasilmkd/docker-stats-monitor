package models

import play.api.libs.json.{ Format, Json }

private[models] trait ContainerDataPlatformSpecific {
  implicit val format: Format[ContainerData] = Json.format

  def apply(line: String): ContainerData = {
    val parts         = line.split(",")
    val cpuPercentage = parts(2).replace("%", "").toDouble
    val memPercentage = parts(4).replace("%", "").toDouble
    ContainerData(parts(0), parts(1), cpuPercentage, parts(3), memPercentage, parts(5), parts(6), parts(7).toInt)
  }
}
