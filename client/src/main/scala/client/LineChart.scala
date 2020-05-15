package client

trait LineChart[F[_]] {
  def update(v: Double): F[Unit]
}
