package client

import cats.effect.{ ExitCode, IO, IOApp }

import client.impl._

object Main extends IOApp {

  implicit private val ioDom      = DOMImpl[IO]
  implicit private val ioCharting = ChartingImpl[IO]

  def run(args: List[String]): IO[ExitCode] =
    WebsocketStream
      .stream[IO]
      .through(new Client[IO].run)
      .compile
      .drain
      .as(ExitCode.Success)
}
