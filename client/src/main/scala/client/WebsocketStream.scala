package client

import cats.effect.{ ConcurrentEffect, Effect, IO, Sync }
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Queue
import io.circe.generic.auto._
import io.circe.parser._
import org.scalajs.dom._

import model._

object WebsocketStream {

  def stream[F[_]: ConcurrentEffect]: Stream[F, Stats] =
    Stream
      .eval {
        for {
          queue <- Queue.unbounded[F, MessageEvent]
          ws    <- Sync[F].delay(new WebSocket("ws://localhost:8080/ws"))
          _     <- Sync[F].delay(ws.onmessage = e => Effect[F].runAsync(queue.enqueue1(e))(_ => IO.unit).unsafeRunSync())
        } yield queue
      }
      .flatMap(_.dequeue)
      .map(_.data.toString)
      .evalMap(decodeStats[F])

  private def decodeStats[F[_]: Sync](json: String): F[Stats] =
    Sync[F].fromEither(decode[Stats](json))
}
