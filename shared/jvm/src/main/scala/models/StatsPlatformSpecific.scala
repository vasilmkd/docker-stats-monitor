package models

import play.api.libs.json.{ Format, Json }

private[models] trait StatsPlatformSpecific {
  implicit val format: Format[Stats] = Json.format
}
