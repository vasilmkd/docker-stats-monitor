package server

import java.io.{ ByteArrayInputStream, InputStream }

import cats.effect.{ Blocker, IO, Sync }
import cats.syntax.all._
import munit.CatsEffectSuite

class StatsDataStreamSuite extends CatsEffectSuite {

  private val lines = List(
    "first line contains column names but is discarded regardless",
    "",
    "977894b9a932,,,optimistic_jang,,,98.00%,,,1.119GiB / 1.944GiB,,,57.57%,,,836B / 0B,,,0B / 0B,,,61",
    "",
    "74cf0440096b,,,friendly_johnson,,,65.30%,,,1.12GiB / 1.944GiB,,,57.59%,,,836B / 0B,,,0B / 0B,,,61",
    "",
    "a651eaf37e82,,,sleepy_tereshkova,,,6.01%,,,1.112GiB / 1.944GiB,,,57.20%,,,766B / 0B,,,0B / 0B,,,61",
    "invalid,,,sleepy_tereshkova,,,6.01%,,,1.112GiB / 1.944GiB,,,57.20%,,,766B / 0B,,,0B / 0B,,,61"
  )

  private val expected =
    lines
      .filter(_.nonEmpty)
      .drop(1)
      .traverse(Stats.parseCSV[Either[Throwable, *]])
      .map(_.map(stats => (stats.id -> stats)).toMap)
      .toOption
      .get
      .filter(_._2.id =!= "")

  private def inputStream[F[_]: Sync](s: String): F[InputStream] =
    Sync[F].delay(new ByteArrayInputStream(s.getBytes()))

  private val blocker = ResourceFixture(Blocker[IO])

  blocker.test("stats data stream") { blocker =>
    StatsDataStream.stream(inputStream[IO](lines.mkString("\n")), blocker).compile.lastOrError.assertEquals(expected)
  }

  blocker.test("invalid data") { blocker =>
    StatsDataStream.stream(inputStream[IO](",\n,"), blocker).compile.drain.attempt.map(_.isLeft).assert
  }
}
