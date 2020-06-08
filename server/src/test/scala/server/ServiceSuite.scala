package server

import java.io.{ File, FileInputStream }

import scala.concurrent.ExecutionContext

import cats.effect.{ Blocker, ContextShift, IO, Timer }
import fs2.concurrent.Topic
import fs2.io._
import fs2.text
import munit.FunSuite
import org.http4s.{ HttpVersion, Method, Request, Status }
import org.http4s.implicits._

import model._

class ServiceSuite extends FunSuite {

  implicit private val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  implicit private val timer: Timer[IO] =
    IO.timer(ExecutionContext.global)

  test("root") {
    val req = Request[IO](Method.GET, uri"/")
    Blocker[IO]
      .use { blocker =>
        for {
          topic <- Topic[IO, DockerData](DockerData.empty)
          res   <- new Service[IO](blocker, topic).routes.orNotFound.run(req)
          _     <- IO(assertEquals(res.status, Status.Ok))
          _     <- IO(assertEquals(res.httpVersion, HttpVersion.`HTTP/1.1`))
          body  <- res.bodyAsText.compile.string
          fis    = IO(new FileInputStream(new File("static/html/index.html")))
          file  <- readInputStream[IO](fis, 8192, blocker).through(text.utf8Decode).compile.string
        } yield assertEquals(body, file)
      }
      .unsafeRunSync()
  }

  test("ws") {
    val req = Request[IO](Method.GET, uri"/ws")
    Blocker[IO]
      .use { blocker =>
        for {
          topic <- Topic[IO, DockerData](DockerData.empty)
          res   <- new Service[IO](blocker, topic).routes.orNotFound.run(req)
          body  <- res.bodyAsText.compile.string
        } yield assertEquals(body, "This is a WebSocket route.")
      }
      .unsafeRunSync()
  }
}
