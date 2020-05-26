package server

import java.io.InputStream

import cats.effect.{ Blocker, ContextShift, Sync }
import cats.implicits._
import fs2.Stream

object StatsDataStream {

  def stream[F[_]: Sync: ContextShift](fis: F[InputStream], blocker: Blocker): Stream[F, StatsData] =
    RawDataStream
      .stream(fis, blocker)
      .evalMap(Stats.parseCSV[F](_))
      .filter(_.id =!= "")
      .fold(StatsData.empty)((map, stats) => map + (stats.id -> stats))
}
