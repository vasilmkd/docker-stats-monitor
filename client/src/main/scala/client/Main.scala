package client

import cats.effect.{ ExitCode, IO, IOApp }

object CatsMain extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    new Client[IO].run.as(ExitCode.Success)
}
