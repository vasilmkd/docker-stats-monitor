package server

import java.io.{ File, FileInputStream }

import cats.effect.IO
import fs2.concurrent.Topic
import fs2.io._
import fs2.text
import munit.CatsEffectSuite
import org.http4s.{ HttpVersion, Method, Request, Status }
import org.http4s.implicits._

import model._

class ServiceSuite extends CatsEffectSuite {

  test("root") {
    val req = Request[IO](Method.GET, uri"/")
    for {
      topic <- Topic[IO, DockerData](DockerData.empty)
      res   <- new Service[IO](topic).routes.orNotFound.run(req)
      _     <- IO(assertEquals(res.status, Status.Ok))
      _     <- IO(assertEquals(res.httpVersion, HttpVersion.`HTTP/1.1`))
      body  <- res.bodyText.compile.string
      fis    = IO(new FileInputStream(new File("static/html/index.html")))
      file  <- readInputStream[IO](fis, 8192).through(text.utf8Decode).compile.string
    } yield assertEquals(body, file)
  }

  test("ws") {
    val req = Request[IO](Method.GET, uri"/ws")
    for {
      topic <- Topic[IO, DockerData](DockerData.empty)
      res   <- new Service[IO](topic).routes.orNotFound.run(req)
      body  <- res.bodyText.compile.string
    } yield assertEquals(body, "This is a WebSocket route.")
  }
}
