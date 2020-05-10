package server

import java.io.File

import cats.effect.{ Blocker, ConcurrentEffect, ContextShift, Timer }
import fs2.{ Pipe, Stream }
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.{ HttpApp, HttpRoutes, StaticFile }
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.staticcontent._
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._

class Server[F[_]: ConcurrentEffect: ContextShift: Timer](blocker: Blocker) extends Http4sDsl[F] {

  val serve: F[Unit] =
    BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(app)
      .serve
      .compile
      .drain

  private lazy val app: HttpApp[F] =
    Router(
      ""       -> rootRoutes,
      "static" -> staticFiles
    ).orNotFound

  private lazy val rootRoutes: HttpRoutes[F] =
    HttpRoutes
      .of[F] {
        case request @ GET -> Root =>
          StaticFile
            .fromFile(new File("static/html/index.html"), blocker, Some(request))
            .getOrElseF(NotFound())
        case GET -> Root / "ws" =>
          val toClient: Stream[F, WebSocketFrame] =
            DockerStats
              .stream[F](blocker)
              .map(s => Text(s.asJson.toString))
          val fromClient: Pipe[F, WebSocketFrame, Unit] = _.map(_ => ())
          WebSocketBuilder[F].build(toClient, fromClient)
      }

  private lazy val staticFiles: HttpRoutes[F] =
    fileService(FileService.Config("static", blocker))
}
