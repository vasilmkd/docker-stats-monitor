package models

import play.api.libs.json.{ Format, Json }

trait StatsPlatformSpecific {
  implicit val format: Format[Stats] = Json.format
}
