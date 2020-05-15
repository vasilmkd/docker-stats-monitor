package client.impl

import cats.effect.Sync

import client.LineChart
import client.chartist._
import client.chartist.Chartist.Line

object LineChartImpl {
  def of[F[_]: Sync](selector: String, initial: Double, options: Options): F[LineChart[F]] =
    Sync[F].delay {
      val data  = new Data(initial)
      val chart = new Line(selector, data, options)
      new LineChart[F] {
        override def update(v: Double): F[Unit] =
          Sync[F].delay {
            data.add(v)
            chart.update(data)
          }
      }
    }
}
