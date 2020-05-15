package client

import cats.effect.{ ExitCode, IO, IOApp }
import fs2.Stream
import fs2.concurrent.Queue
import org.scalajs.dom._

object Main extends IOApp {

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
      .through(new Client[IO].run)
      .compile
      .drain
      .as(ExitCode.Success)
}
