package server

import cats.effect.{ IO, Resource }
import cats.syntax.all._
import fs2.io._
import fs2.text
import munit.CatsEffectSuite

class DockerIntegrationSuite extends CatsEffectSuite {

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

  private val runContainer: IO[String] = {
    val fis = IO(dockerRun.start().getInputStream())
    readInputStream(fis, 8192)
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

  private val containerResource: Resource[IO, String] =
    Resource.make(runContainer)(stopContainer)

  private val container = ResourceFixture(containerResource)

  container.test("integration") { id =>
    DockerDataStream
      .stream[IO]
      .take(3)
      .compile
      .toList
      .map(_.map(_.find(_.id === id).get))
      .map {
        _.foreach { data =>
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
}
