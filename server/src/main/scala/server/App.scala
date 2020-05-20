package server

import scala.concurrent.ExecutionContext

import cats.effect.{ Blocker, ExitCode, IO, IOApp }
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

object App extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    Blocker[IO]
      .use { blocker =>
        BlazeServerBuilder[IO](ExecutionContext.global)
          .bindHttp(8080, "0.0.0.0")
          .withHttpApp(new Service[IO](blocker).routes.orNotFound)
          .serve
          .compile
          .drain
      }
      .as(ExitCode.Success)
}
