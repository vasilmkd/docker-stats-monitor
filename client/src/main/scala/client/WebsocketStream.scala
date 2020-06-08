package client

import cats.Show
import cats.effect.{ ConcurrentEffect, Effect, IO, Sync }
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Queue
import io.circe.generic.auto._
import io.circe.parser._
import org.scalajs.dom._

import model._

object WebsocketStream {

  def stream[F[_]: ConcurrentEffect]: Stream[F, DockerData] =
    Stream
      .eval {
        for {
          queue <- Queue.unbounded[F, MessageEvent]
          host  <- Sync[F].delay(window.location.host)
          ws    <- Sync[F].delay(new WebSocket(s"ws://$host/ws"))
          _     <- Sync[F].delay(ws.onmessage = e => Effect[F].runAsync(queue.enqueue1(e))(_ => IO.unit).unsafeRunSync())
        } yield queue
      }
      .flatMap(_.dequeue)
      .map(_.data.show)
      .evalMap(decodeDockerData[F])

  private def decodeDockerData[F[_]: Sync](json: String): F[DockerData] =
    Sync[F].fromEither(decode[DockerData](json))

  implicit private val anyShow: Show[Any] =
    Show.fromToString
}
