package server

import scala.concurrent.ExecutionContext

import cats.effect.{ Blocker, ContextShift, IO, Resource, Timer }
import cats.implicits._
import fs2.io._
import fs2.text
import munit.FunSuite

class DockerIntegrationSuite extends FunSuite {

  implicit private val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  implicit private val timer: Timer[IO] =
    IO.timer(ExecutionContext.global)

  private val dockerRun = new ProcessBuilder()
    .command(
      "docker",
      "run",
      "-d",
      "--rm",
      "-v",
      "/var/run/docker.sock:/var/run/docker.sock",
      "vasilvasilev97/docker-stats-monitor"
    )

  private def runContainer(blocker: Blocker): IO[String] = {
    val fis = IO(dockerRun.start().getInputStream())
    readInputStream(fis, 8192, blocker)
      .through(text.utf8Decode)
      .compile
      .string
      .map(_.substring(0, 12))
  }

  private def dockerStop(id: String): ProcessBuilder =
    new ProcessBuilder()
      .command("docker", "stop", id)

  private def stopContainer(id: String): IO[Unit] =
    IO(dockerStop(id).start().waitFor()).void

  private def withContainer(blocker: Blocker): Resource[IO, String] =
    Resource.make(runContainer(blocker))(stopContainer)

  test("integration") {
    val blockerAndId = for {
      blocker <- Blocker[IO]
      id      <- withContainer(blocker)
    } yield (blocker, id)

    blockerAndId
      .use {
        case (blocker, id) =>
          DockerDataStream
            .stream[IO](blocker)
            .take(3)
            .compile
            .toList
            .map(_.map(_.find(_.id === id).get))
      }
      .unsafeRunSync()
      .foreach { data =>
        assert(data.name.nonEmpty)
        assertEquals(data.image, "vasilvasilev97/docker-stats-monitor")
        assert(data.runningFor.nonEmpty)
        assert(data.status.nonEmpty)
        assert(data.cpuPercentage >= 0)
        assert(data.memUsage.nonEmpty)
        assert(data.memPercentage >= 0)
        assert(data.netIO.nonEmpty)
        assert(data.blockIO.nonEmpty)
        assert(data.pids >= 0)
        assert(data.size.nonEmpty)
        assert(data.ports.nonEmpty)
      }
  }
}
