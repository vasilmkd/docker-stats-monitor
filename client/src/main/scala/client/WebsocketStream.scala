package client

import cats.Show
import cats.effect.{ Async, Sync }
import cats.effect.std.{ Dispatcher, Queue }
import cats.syntax.all._
import fs2.Stream
import io.circe.generic.auto._
import io.circe.parser._
import org.scalajs.dom._

import model._

object WebsocketStream {

  def stream[F[_]: Async]: Stream[F, DockerData] =
    Stream
      .resource(Dispatcher[F])
      .evalMap { dispatcher =>
        for {
          queue <- Queue.circularBuffer[F, MessageEvent](1)
          host  <- Sync[F].delay(window.location.host)
          ws    <- Sync[F].delay(new WebSocket(s"ws://$host/ws"))
          _     <- Sync[F].delay(ws.onmessage = e => dispatcher.unsafeRunAndForget(queue.offer(e)))
        } yield queue
      }
      .flatMap(q => Stream.repeatEval(q.take))
      .map(_.data.show)
      .evalMap(decodeDockerData[F])

  private def decodeDockerData[F[_]: Sync](json: String): F[DockerData] =
    Sync[F].fromEither(decode[DockerData](json))

  implicit private val anyShow: Show[Any] =
    Show.fromToString
}
