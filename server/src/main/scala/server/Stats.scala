package server

import scala.util.Try

import cats.{ Applicative, MonadError }
import cats.implicits._

final case class Stats(
  id: String,
  name: String,
  cpuPercentage: Double,
  memUsage: String,
  memPercentage: Double,
  netIO: String,
  blockIO: String,
  pids: Int
)

object Stats {
  def parseCSV[F[_]: MonadError[*[_], Throwable]](line: String): F[Stats] =
    for {
      parts <- splitLine[F](line)
      stats <- parseParts[F](parts)
    } yield stats

  private def splitLine[F[_]: MonadError[*[_], Throwable]](line: String): F[Array[String]] =
    for {
      parts <- Applicative[F].pure(line.split(",,,"))
      _     <- MonadError[F, Throwable]
                 .raiseError(new IllegalStateException(s"Invalid docker stats data csv: $line"))
                 .whenA(parts.length < 8)
    } yield parts

  private def parseParts[F[_]: MonadError[*[_], Throwable]](parts: Array[String]): F[Stats] =
    MonadError[F, Throwable].catchNonFatal {
      val cpuPercentage = Try(parts(2).replace("%", "").toDouble).toOption.getOrElse(0.0)
      val memPercentage = Try(parts(4).replace("%", "").toDouble).toOption.getOrElse(0.0)
      Stats(
        Try(parts(0).substring(0, 12)).toOption.getOrElse(""),
        parts(1),
        cpuPercentage,
        parts(3),
        memPercentage,
        parts(5),
        parts(6),
        Try(parts(7).toInt).toOption.getOrElse(0)
      )
    }
}
