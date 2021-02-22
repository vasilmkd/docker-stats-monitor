package server

import java.io.InputStream

import cats.effect.Sync
import fs2.{ text, Stream }
import fs2.io._

private object RawDataStream {

  def stream[F[_]: Sync](fis: F[InputStream]): Stream[F, String] =
    readInputStream(fis, 8192)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(_.nonEmpty)
      .drop(1)
}
