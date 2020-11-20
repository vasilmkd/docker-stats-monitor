package server

import scala.concurrent.duration._

import cats.Functor
import cats.effect.{ Blocker, ContextShift, Sync, Timer }
import fs2.{ Pipe, Stream }

import model._

object DockerDataStream {

  def stream[F[_]: Sync: ContextShift: Timer](blocker: Blocker): Stream[F, DockerData] =
    ticker(
      StatsDataStream
        .stream[F](DockerStats.input, blocker)
        .zip(ProcessesDataStream.stream[F](DockerProcesses.input, blocker))
        .through(combiner)
    )

  private def combiner[F[_]]: Pipe[F, (StatsData, ProcessesData), DockerData] =
    _.map { case (statsData, processesData) =>
      statsData
        .map { case (id, stats) =>
          val processes                                                                    = processesData.get(id).getOrElse(Processes(id, "", "", "", "", ""))
          val Stats(_, name, cpuPercentage, memUsage, memPercentage, netIO, blockIO, pids) = stats
          val Processes(_, image, created, ports, status, size)                            = processes
          val mappedPorts                                                                  = if (ports.isEmpty) "No mapped ports" else ports
          ContainerData(
            id,
            name,
            image,
            created,
            status,
            cpuPercentage,
            memUsage,
            memPercentage,
            netIO,
            blockIO,
            pids,
            size,
            mappedPorts
          )
        }
        .toSet[ContainerData]
    }

  private def ticker[F[_]: Functor: Timer, A](stream: Stream[F, A]): Stream[F, A] =
    (Stream.emit(Duration.Zero) ++ Stream.awakeEvery[F](5.seconds))
      .as(stream)
      .flatten
}
