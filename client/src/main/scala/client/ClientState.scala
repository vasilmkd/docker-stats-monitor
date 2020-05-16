package client

import cats.MonadError

import model._

final case class ClientState[F[_]: MonadError[*[_], Throwable]](private[client] val map: Map[String, ChartState[F]]) {

  def +(pair: (String, ChartState[F])): ClientState[F] =
    ClientState(map + pair)

  def -(key: String): ClientState[F] =
    ClientState(map - key)

  def get(id: String): F[ChartState[F]] =
    MonadError[F, Throwable].catchNonFatal(map(id))

  def partition(stats: Stats): ClientState.Partition = {
    val keySet   = map.keySet
    val statsIds = stats.data.map(_.id)

    val removed = keySet.diff(statsIds)
    val added   = statsIds.diff(keySet)
    val updated = keySet.intersect(statsIds)

    ClientState.Partition(removed, added, updated)
  }
}

object ClientState {
  case class Partition(removed: Set[String], added: Set[String], updated: Set[String])

  def empty[F[_]: MonadError[*[_], Throwable]]: ClientState[F] = ClientState(Map.empty)
}
