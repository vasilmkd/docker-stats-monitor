package client

final case class ChartState[F[_]](cpuChart: LineChart[F], memChart: LineChart[F])
