package server

import java.io.InputStream

import scala.concurrent.duration._

import cats.Functor
import cats.effect.{ Blocker, ContextShift, Sync, Timer }
import fs2.{ text, Stream }
import fs2.io._

import model._

object StatsStream {

  def stream[F[_]: Sync: ContextShift: Timer](fis: F[InputStream], blocker: Blocker): Stream[F, Stats] =
    ticker(statsStream(fis, blocker))

  private def ticker[F[_]: Functor: Timer, A](stream: Stream[F, A]): Stream[F, A] =
    (Stream.emit(Duration.Zero) ++ Stream.awakeEvery[F](5.seconds))
      .as(stream)
      .flatten

  private def statsStream[F[_]: Sync: ContextShift](fis: F[InputStream], blocker: Blocker): Stream[F, Stats] =
    readInputStream(fis, 8192, blocker)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.nonEmpty)
      .drop(1)
      .evalMap(ContainerData.parseCSV(_))
      .fold(Set.empty[ContainerData])(_ + _)
      .map(Stats(_))
}
