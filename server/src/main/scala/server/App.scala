package server

import scala.concurrent.ExecutionContext.global

import cats.effect.{ Blocker, ExitCode, IO, IOApp }

object App extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    Blocker[IO].use(new Server[IO](_, global).serve).as(ExitCode.Success)
}
