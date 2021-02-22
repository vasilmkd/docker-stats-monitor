package client

import cats.effect.{ IO, IOApp }

import client.impl._

object Main extends IOApp.Simple {

  implicit private val ioDom      = DOMImpl[IO]
  implicit private val ioCharting = ChartingImpl[IO]

  def run: IO[Unit] =
    WebsocketStream
      .stream[IO]
      .through(new Client[IO].run)
      .compile
      .drain
}
