package server

import java.io.InputStream

import cats.effect.{ Blocker, ContextShift, Sync, Timer }
import fs2.{ text, Stream }
import fs2.io._

object StatsDataStream {

  def stream[F[_]: Sync: ContextShift: Timer](fis: F[InputStream], blocker: Blocker): Stream[F, StatsData] =
    readInputStream(fis, 8192, blocker)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.nonEmpty)
      .drop(1)
      .evalMap(Stats.parseCSV(_))
      .fold(StatsData.empty)((map, stats) => map + (stats.id -> stats))
}
