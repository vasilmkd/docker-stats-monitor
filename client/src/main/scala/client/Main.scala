package client

import cats.effect.{ ExitCode, IO, IOApp }
import fs2.Stream
import fs2.concurrent.Queue
import io.circe.generic.auto._
import io.circe.parser._
import org.scalajs.dom._

import client.impl._
import models._

object Main extends IOApp {

  implicit private val ioDom      = DOMImpl[IO]
  implicit private val ioCharting = ChartingImpl[IO]

  def run(args: List[String]): IO[ExitCode] =
    Stream
      .eval {
        for {
          queue <- Queue.unbounded[IO, MessageEvent]
          ws    <- IO(new WebSocket("ws://localhost:8080/ws"))
          _ <- IO {
                ws.onmessage = e => queue.enqueue1(e).unsafeRunSync()
              }
        } yield queue
      }
      .flatMap(queue => queue.dequeue)
      .map(_.data.toString)
      .evalMap(decodeStats)
      .through(new Client[IO].run)
      .compile
      .drain
      .as(ExitCode.Success)

  private def decodeStats(json: String): IO[Stats] =
    IO.fromEither(decode[Stats](json))
}
