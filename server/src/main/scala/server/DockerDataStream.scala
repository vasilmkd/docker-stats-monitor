package server

import scala.concurrent.duration._

import cats.effect.{ Sync, Temporal }
import fs2.{ Pipe, Stream }

import model._

object DockerDataStream {

  def stream[F[_]: Sync: Temporal]: Stream[F, DockerData] =
    ticker(
      StatsDataStream
        .stream[F](DockerStats.input)
        .zip(ProcessesDataStream.stream[F](DockerProcesses.input))
        .through(combiner)
    )

  private def combiner[F[_]]: Pipe[F, (StatsData, ProcessesData), DockerData] =
    _.map {
      case (statsData, processesData) =>
        statsData
          .map {
            case (id, stats) =>
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

  private def ticker[F[_]: Temporal, A](stream: Stream[F, A]): Stream[F, A] =
    (Stream.emit(Duration.Zero) ++ Stream.awakeEvery[F](5.seconds))
      .as(stream)
      .flatten
}
