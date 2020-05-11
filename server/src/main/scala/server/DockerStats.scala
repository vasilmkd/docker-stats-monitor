package server

import java.io.InputStream

import scala.concurrent.duration._

import cats.effect.{ Blocker, ContextShift, Sync, Timer }
import fs2.{ text, Stream }
import fs2.io._

import models._

object DockerStats {

  def stream[F[_]: Sync: ContextShift: Timer](blocker: Blocker): Stream[F, Stats] =
    ticker(statsStream(blocker))

  private def ticker[F[_]: Sync: Timer, A](stream: Stream[F, A]): Stream[F, A] =
    Stream
      .awakeEvery[F](5.seconds)
      .map(_ => stream)
      .flatten

  private def statsStream[F[_]: Sync: ContextShift](blocker: Blocker): Stream[F, Stats] =
    readInputStream(processInputStream(statsBuilder), 8192, blocker)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.nonEmpty)
      .drop(1)
      .evalMap(ContainerData.parse(_))
      .fold(Stats(Set.empty))((s, cd) => Stats(s.data + cd))

  private def processInputStream[F[_]: Sync](pb: ProcessBuilder): F[InputStream] =
    Sync[F].delay(pb.start().getInputStream())

  private val statsBuilder = new ProcessBuilder()
    .command(
      "docker",
      "stats",
      "--format",
      "table {{.ID}},{{.Name}},{{.CPUPerc}},{{.MemUsage}},{{.MemPerc}},{{.NetIO}},{{.BlockIO}},{{.PIDs}}",
      "--no-stream"
    )
}
