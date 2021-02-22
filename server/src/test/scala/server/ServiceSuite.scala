package server

import java.io.{ File, FileInputStream }

import cats.effect.{ Blocker, IO }
import fs2.concurrent.Topic
import fs2.io._
import fs2.text
import munit.CatsEffectSuite
import org.http4s.{ HttpVersion, Method, Request, Status }
import org.http4s.implicits._

import model._

class ServiceSuite extends CatsEffectSuite {

  private val blocker = ResourceFixture(Blocker[IO])

  blocker.test("root") { blocker =>
    val req = Request[IO](Method.GET, uri"/")
    for {
      topic <- Topic[IO, DockerData](DockerData.empty)
      res   <- new Service[IO](blocker, topic).routes.orNotFound.run(req)
      _     <- IO(assertEquals(res.status, Status.Ok))
      _     <- IO(assertEquals(res.httpVersion, HttpVersion.`HTTP/1.1`))
      body  <- res.bodyText.compile.string
      fis    = IO(new FileInputStream(new File("static/html/index.html")))
      file  <- readInputStream[IO](fis, 8192, blocker).through(text.utf8Decode).compile.string
    } yield assertEquals(body, file)
  }

  blocker.test("ws") { blocker =>
    val req = Request[IO](Method.GET, uri"/ws")
    for {
      topic <- Topic[IO, DockerData](DockerData.empty)
      res   <- new Service[IO](blocker, topic).routes.orNotFound.run(req)
      body  <- res.bodyText.compile.string
    } yield assertEquals(body, "This is a WebSocket route.")
  }
}
