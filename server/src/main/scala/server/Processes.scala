package server

import scala.util.Try

import cats.{ Applicative, MonadError }
import cats.implicits._

final case class Processes(
  id: String,
  image: String,
  created: String,
  ports: String,
  status: String,
  size: String
)

object Processes {
  def parseCSV[F[_]: MonadError[*[_], Throwable]](line: String): F[Processes] =
    for {
      parts     <- splitLine[F](line)
      processes <- parseParts[F](parts)
    } yield processes

  private def splitLine[F[_]: MonadError[*[_], Throwable]](line: String): F[Array[String]] =
    for {
      parts <- Applicative[F].pure(line.split(",,,"))
      _     <- MonadError[F, Throwable]
                 .raiseError(new IllegalStateException(s"Invalid docker ps data csv: $line"))
                 .whenA(parts.length < 6)
    } yield parts

  private def parseParts[F[_]: MonadError[*[_], Throwable]](parts: Array[String]): F[Processes] =
    MonadError[F, Throwable].catchNonFatal {
      val id = Try(parts(0).substring(0, 12)).toOption.getOrElse("")
      Processes(id, parts(1), parts(2), parts(3), parts(4), parts(5))
    }
}
