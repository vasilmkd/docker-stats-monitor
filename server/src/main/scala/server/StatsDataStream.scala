package server

import java.io.InputStream

import cats.effect.Sync
import cats.syntax.all._
import fs2.Stream

object StatsDataStream {

  def stream[F[_]: Sync](fis: F[InputStream]): Stream[F, StatsData] =
    RawDataStream
      .stream(fis)
      .evalMap(Stats.parseCSV[F](_))
      .filter(_.id =!= "")
      .fold(StatsData.empty)((map, stats) => map + (stats.id -> stats))
}
