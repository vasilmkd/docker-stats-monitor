package server

import scala.concurrent.ExecutionContext

import cats.effect.{ Blocker, ContextShift, IO, Resource, Timer }
import cats.implicits._
import munit.FunSuite

class DockerStatsSuite extends FunSuite {

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

  test("docker stats") {
    val blocker = for {
      _       <- withContainer
      blocker <- Blocker[IO]
    } yield blocker

    blocker
      .use { blocker =>
        StatsStream
          .stream(DockerStats.input[IO], blocker)
          .take(3)
          .compile
          .toList
      }
      .unsafeRunSync()
      .foreach { stats =>
        val data = stats.data.find(_.name === "monitor").get
        assert(data.id.nonEmpty)
        assertEquals(data.name, "monitor")
        assert(data.cpuPercentage >= 0)
        assert(data.memUsage.nonEmpty)
        assert(data.memPercentage >= 0)
        assert(data.netIO.nonEmpty)
        assert(data.blockIO.nonEmpty)
        assert(data.pids >= 0)
      }
  }
}
