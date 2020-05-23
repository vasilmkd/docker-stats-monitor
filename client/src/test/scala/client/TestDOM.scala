package client

import cats.Applicative
import model.ContainerData

object TestDOM {
  def apply[F[_]: Applicative]: DOM[F] =
    new DOM[F] {
      override def onAdded(cd: ContainerData): F[Unit]   =
        Applicative[F].unit
      override def onRemoved(id: String): F[Unit]        =
        Applicative[F].unit
      override def onUpdated(cd: ContainerData): F[Unit] =
        Applicative[F].unit
    }
}
