package server

import java.io.{ ByteArrayInputStream, InputStream }

import scala.concurrent.ExecutionContext

import cats.effect.{ Blocker, ContextShift, IO, Sync }
import cats.implicits._
import munit.FunSuite

class ProcessesDataStreamSuite extends FunSuite {

  implicit private val contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)

  private val lines = List(
    "first line contains column names but is discarded regardless",
    "",
    "8c2f7598fc14,,,cassandra:3.11.6,,,4 hours ago,,,7000-7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp,,,Up 4 hours,,,893kB (virtual 379MB)",
    "",
    "8d4ae1776df1,,,ubuntu,,,35 seconds ago,,,,,,Up 33 seconds,,,0B (virtual 64.2MB)"
  )

  private val expected =
    lines
      .filter(_.nonEmpty)
      .drop(1)
      .traverse(Processes.parseCSV[Either[Throwable, *]])
      .map(_.map(ps => (ps.id -> ps)).toMap)
      .toOption
      .get

  private def inputStream[F[_]: Sync](s: String): F[InputStream] =
    Sync[F].delay(new ByteArrayInputStream(s.getBytes()))

  test("processes data stream") {
    val test = Blocker[IO].use { blocker =>
      ProcessesDataStream.stream(inputStream[IO](lines.mkString("\n")), blocker).compile.lastOrError
    }
    assertEquals(test.unsafeRunSync(), expected)
  }

  test("invalid data") {
    val test =
      Blocker[IO].use(blocker => ProcessesDataStream.stream(inputStream[IO](",\n,"), blocker).compile.drain).attempt
    assert(test.unsafeRunSync().isLeft)
  }
}
