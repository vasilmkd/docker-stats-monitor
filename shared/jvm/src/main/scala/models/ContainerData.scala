package models

import play.api.libs.json.{ Format, Json }

trait ContainerDataPlatformSpecific {
  implicit val format: Format[ContainerData] = Json.format

  def apply(line: String): ContainerData = {
    val parts = line.split(",")
    ContainerData(parts(0), parts(1), parts(2).replace("%", "").toDouble, parts(3).replace("%", "").toDouble)
  }
}
