package server

import java.io.InputStream

import cats.effect.Sync
import cats.syntax.all._
import fs2.Stream

object ProcessesDataStream {

  def stream[F[_]: Sync](fis: F[InputStream]): Stream[F, ProcessesData] =
    RawDataStream
      .stream(fis)
      .evalMap(Processes.parseCSV[F](_))
      .filter(_.id =!= "")
      .fold(ProcessesData.empty)((map, ps) => map + (ps.id -> ps))
}
