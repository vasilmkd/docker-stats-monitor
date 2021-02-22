package server

import java.io.File

import cats.effect.Async
import cats.syntax.all._
import fs2.{ Pipe, Stream }
import fs2.concurrent.Topic
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{ HttpRoutes, StaticFile }
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.staticcontent._
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

import model._

class Service[F[_]: Async](topic: Topic[F, DockerData]) extends Http4sDsl[F] {

  val routes: HttpRoutes[F] =
    Router(
      ""       -> rootRoutes,
      "static" -> staticFiles
    )

  private lazy val rootRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case request @ GET -> Root =>
        StaticFile
          .fromFile(new File("static/html/index.html"), Some(request))
          .getOrElseF(NotFound())

      case GET -> Root / "ws"    =>
        val toClient: Stream[F, WebSocketFrame]       =
          topic
            .subscribe(1)
            .map(s => WebSocketFrame.Text(s.asJson.show))
        val fromClient: Pipe[F, WebSocketFrame, Unit] = _.as(())
        WebSocketBuilder[F].build(toClient, fromClient)
    }

  private lazy val staticFiles: HttpRoutes[F] =
    fileService(FileService.Config("static"))
}
