package client

import model._

trait DOM[F[_]] {
  def onAdded(cd: ContainerData): F[Unit]
  def onRemoved(id: String): F[Unit]
  def onUpdated(cd: ContainerData): F[Unit]
}

object DOM {
  def apply[F[_]: DOM]: DOM[F] = implicitly
}
