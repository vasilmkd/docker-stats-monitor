package client.impl

import cats.effect.Sync

import client._
import client.chartist.Options

object ChartingImpl {
  implicit def apply[F[_]: Sync]: Charting[F] =
    new Charting[F] {
      override def createLineChart(selector: String, initial: Double, options: Options): F[LineChart[F]] =
        LineChartImpl.of[F](selector, initial, options)
    }
}
