package server

import cats.MonadError
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
      parts <- line.split(",").pure[F]
      _ <- MonadError[F, Throwable]
            .raiseError(new IllegalStateException(s"Invalid container data csv: $line"))
            .whenA(parts.length < 8)
    } yield parts

  private def parseParts[F[_]: MonadError[*[_], Throwable]](parts: Array[String]): F[Stats] =
    MonadError[F, Throwable].catchNonFatal {
      val cpuPercentage = parts(2).replace("%", "").toDouble
      val memPercentage = parts(4).replace("%", "").toDouble
      Stats(parts(0), parts(1), cpuPercentage, parts(3), memPercentage, parts(5), parts(6), parts(7).toInt)
    }
}
