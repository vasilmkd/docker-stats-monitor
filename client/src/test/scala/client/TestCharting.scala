package client

import cats.Applicative
import client.chartist.Options

object TestCharting {
  def apply[F[_]: Applicative]: Charting[F] =
    new Charting[F] {
      override def createLineChart(selector: String, initial: Double, options: Options): F[LineChart[F]] =
        Applicative[F].pure(new TestLineChart[F])
    }
}
