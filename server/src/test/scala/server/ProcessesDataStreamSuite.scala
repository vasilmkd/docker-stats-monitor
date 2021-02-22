package server

import java.io.{ ByteArrayInputStream, InputStream }

import cats.effect.{ Blocker, IO, Sync }
import cats.syntax.all._
import munit.CatsEffectSuite

class ProcessesDataStreamSuite extends CatsEffectSuite {

  private val lines = List(
    "first line contains column names but is discarded regardless",
    "",
    "8c2f7598fc14,,,cassandra:3.11.6,,,4 hours ago,,,7000-7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp,,,Up 4 hours,,,893kB (virtual 379MB)",
    "",
    "8d4ae1776df1,,,ubuntu,,,35 seconds ago,,,,,,Up 33 seconds,,,0B (virtual 64.2MB)",
    "invalid,,,ubuntu,,,35 seconds ago,,,,,,Up 33 seconds,,,0B (virtual 64.2MB)"
  )

  private val expected =
    lines
      .filter(_.nonEmpty)
      .drop(1)
      .traverse(Processes.parseCSV[Either[Throwable, *]])
      .map(_.map(ps => (ps.id -> ps)).toMap)
      .toOption
      .get
      .filter(_._2.id =!= "")

  private def inputStream[F[_]: Sync](s: String): F[InputStream] =
    Sync[F].delay(new ByteArrayInputStream(s.getBytes()))

  private val blocker = ResourceFixture(Blocker[IO])

  blocker.test("processes data stream") { blocker =>
    ProcessesDataStream
      .stream(inputStream[IO](lines.mkString("\n")), blocker)
      .compile
      .lastOrError
      .assertEquals(expected)
  }

  blocker.test("invalid data") { blocker =>
    ProcessesDataStream.stream(inputStream[IO](",\n,"), blocker).compile.drain.attempt.map(_.isLeft).assert
  }
}
