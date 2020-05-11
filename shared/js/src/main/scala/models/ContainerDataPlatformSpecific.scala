package models

import scala.scalajs.js

import cats.MonadError

private[models] trait ContainerDataPlatformSpecific {
  def parse[F[_]: MonadError[*[_], Throwable]](obj: js.Dynamic): F[ContainerData] =
    MonadError[F, Throwable].catchNonFatal {
      val id            = obj.id.asInstanceOf[js.UndefOr[String]]
      val name          = obj.name.asInstanceOf[js.UndefOr[String]]
      val cpuPercentage = obj.cpuPercentage.asInstanceOf[js.UndefOr[Double]]
      val memUsage      = obj.memUsage.asInstanceOf[js.UndefOr[String]]
      val memPercentage = obj.memPercentage.asInstanceOf[js.UndefOr[Double]]
      val netIO         = obj.netIO.asInstanceOf[js.UndefOr[String]]
      val blockIO       = obj.blockIO.asInstanceOf[js.UndefOr[String]]
      val pids          = obj.pids.asInstanceOf[js.UndefOr[Int]]
      ContainerData(
        id.get,
        name.get,
        cpuPercentage.get,
        memUsage.get,
        memPercentage.get,
        netIO.get,
        blockIO.get,
        pids.get
      )
    }
}
