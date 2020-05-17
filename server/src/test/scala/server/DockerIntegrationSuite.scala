package server

import scala.concurrent.ExecutionContext

import cats.effect.{ Blocker, ContextShift, IO, Resource, Timer }
import cats.implicits._
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
      "--name",
      "monitor",
      "-p",
      "8080:8080",
      "-v",
      "/var/run/docker.sock:/var/run/docker.sock",
      "vasilvasilev97/docker-stats-monitor"
    )

  private val runProcess: IO[Unit] =
    IO(dockerRun.start().waitFor()).void

  private val dockerStop = new ProcessBuilder()
    .command("docker", "stop", "monitor")

  private val stopProcess: IO[Unit] =
    IO(dockerStop.start().waitFor()).void

  private val withContainer: Resource[IO, Unit] =
    Resource.make(runProcess)(_ => stopProcess)

  test("integration") {
    val blocker = for {
      _       <- withContainer
      blocker <- Blocker[IO]
    } yield blocker

    blocker
      .use { blocker =>
        DockerDataStream
          .stream[IO](blocker)
          .take(3)
          .compile
          .toList
      }
      .unsafeRunSync()
      .foreach { data =>
        val cd = data.find(_.name === "monitor").get
        assert(cd.id.nonEmpty)
        assertEquals(cd.name, "monitor")
        assertEquals(cd.image, "vasilvasilev97/docker-stats-monitor")
        assert(cd.runningFor.nonEmpty)
        assert(cd.status.nonEmpty)
        assert(cd.cpuPercentage >= 0)
        assert(cd.memUsage.nonEmpty)
        assert(cd.memPercentage >= 0)
        assert(cd.netIO.nonEmpty)
        assert(cd.blockIO.nonEmpty)
        assert(cd.pids >= 0)
        assert(cd.size.nonEmpty)
        assert(cd.ports.nonEmpty)
      }
  }
}
