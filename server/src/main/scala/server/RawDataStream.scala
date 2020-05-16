package server

import java.io.InputStream

import cats.effect.{ Blocker, ContextShift, Sync }
import fs2.{ text, Stream }
import fs2.io._

private object RawDataStream {

  def stream[F[_]: Sync: ContextShift](fis: F[InputStream], blocker: Blocker): Stream[F, String] =
    readInputStream(fis, 8192, blocker)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.nonEmpty)
      .drop(1)
}
