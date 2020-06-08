package server

import scala.concurrent.ExecutionContext

import cats.effect.{ Blocker, ExitCode, IO, IOApp }
import fs2.concurrent.Topic
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import model._

object App extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    Blocker[IO]
      .use { blocker =>
        for {
          topic <- Topic[IO, DockerData](DockerData.empty)
          _     <- DockerDataStream.stream[IO](blocker).through(topic.publish).compile.drain.start
          _     <- BlazeServerBuilder[IO](ExecutionContext.global)
                     .bindHttp(8080, "0.0.0.0")
                     .withHttpApp(new Service(blocker, topic).routes.orNotFound)
                     .serve
                     .compile
                     .drain
        } yield ()
      }
      .as(ExitCode.Success)
}
