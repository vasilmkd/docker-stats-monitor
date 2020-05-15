package client

import client.chartist.Options

trait Charting[F[_]] {
  def createLineChart(selector: String, initial: Double, options: Options): F[LineChart[F]]
}

object Charting {
  def apply[F[_]: Charting]: Charting[F] = implicitly
}
