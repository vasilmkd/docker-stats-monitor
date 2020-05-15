package client

import cats.Applicative

class TestLineChart[F[_]: Applicative] extends LineChart[F] {
  override def update(v: Double): F[Unit] =
    Applicative[F].unit
}
