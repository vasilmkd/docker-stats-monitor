package controllers

import javax.inject.{ Inject, Singleton }

import scala.concurrent.duration._

import akka.NotUsed
import akka.stream.scaladsl.{ Flow, Framing, Sink, Source, StreamConverters }
import akka.util.ByteString
import play.api.libs.json.Json
import play.api.mvc.{ BaseController, ControllerComponents, WebSocket }

import models._

@Singleton
class WsController @Inject() (override val controllerComponents: ControllerComponents) extends BaseController {

  private val builder = new ProcessBuilder()
    .command(
      "docker",
      "stats",
      "--format",
      "table {{.ID}},{{.Name}},{{.CPUPerc}},{{.MemPerc}}",
      "--no-stream",
      "--no-trunc"
    )

  private val source: Source[Stats, NotUsed] =
    StreamConverters
      .fromInputStream(() => builder.start().getInputStream())
      .via(Framing.delimiter(ByteString("\n"), 8192, true))
      .map(_.utf8String)
      .drop(1)
      .map(ContainerData(_))
      .fold(Stats(Set.empty))((s, cd) => Stats(s.data + cd))
      .mapMaterializedValue(_ => NotUsed)

  def stats(): WebSocket = WebSocket.accept { request =>
    val in = Sink.foreach(println)
    val out = Source
      .tick(Duration.Zero, 5.seconds, source.map(Json.toJson(_)))
      .flatMapConcat(identity)
      .mapMaterializedValue(_ => NotUsed)
    Flow.fromSinkAndSource(in, out)
  }
}
