package models

import cats.MonadError

private[models] trait ContainerDataPlatformSpecific {
  def parse[F[_]: MonadError[*[_], Throwable]](line: String): F[ContainerData] =
    MonadError[F, Throwable].catchNonFatal {
      val parts         = line.split(",")
      val cpuPercentage = parts(2).replace("%", "").toDouble
      val memPercentage = parts(4).replace("%", "").toDouble
      ContainerData(parts(0), parts(1), cpuPercentage, parts(3), memPercentage, parts(5), parts(6), parts(7).toInt)
    }
}
