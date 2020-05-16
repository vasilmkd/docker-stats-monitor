package server

import scala.concurrent.duration._

import cats.Functor
import cats.effect.{ Blocker, ContextShift, Sync, Timer }
import fs2.{ Pipe, Stream }

import model._

object DockerDataStream {

  def stream[F[_]: Sync: ContextShift: Timer](blocker: Blocker): Stream[F, DockerData] =
    ticker(StatsDataStream.stream[F](DockerStats.input[F], blocker).through(combiner))

  private def combiner[F[_]]: Pipe[F, StatsData, DockerData] =
    _.map { statsData =>
      statsData.values
        .map { stats =>
          val Stats(id, name, cpuPercentage, memUsage, memPercentage, netIO, blockIO, pids) = stats
          ContainerData(id, name, cpuPercentage, memUsage, memPercentage, netIO, blockIO, pids)
        }
        .toSet[ContainerData]
    }

  private def ticker[F[_]: Functor: Timer, A](stream: Stream[F, A]): Stream[F, A] =
    (Stream.emit(Duration.Zero) ++ Stream.awakeEvery[F](5.seconds))
      .as(stream)
      .flatten
}
