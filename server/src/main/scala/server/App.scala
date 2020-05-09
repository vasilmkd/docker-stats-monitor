package server

import cats.implicits._
import cats.effect.{ Blocker, ExitCode, IO, IOApp }

object App extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    Blocker[IO].use(new Server[IO](_).serve).as(ExitCode.Success)
}
