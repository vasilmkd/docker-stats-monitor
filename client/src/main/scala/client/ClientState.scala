package client

import model._

final case class ClientState[F[_]](private[client] val map: Map[String, ChartState[F]]) {

  def +(pair: (String, ChartState[F])): ClientState[F] =
    ClientState(map + pair)

  def -(key: String): ClientState[F] =
    ClientState(map - key)

  def get(id: String): Option[ChartState[F]] =
    map.get(id)

  def partition(data: DockerData): ClientState.Partition = {
    val keySet       = map.keySet
    val containerIds = data.map(_.id)

    val removed = keySet.diff(containerIds)
    val added   = containerIds.diff(keySet)
    val updated = keySet.intersect(containerIds)

    ClientState.Partition(removed, added, updated)
  }
}

object ClientState {
  case class Partition(removed: Set[String], added: Set[String], updated: Set[String])

  def empty[F[_]]: ClientState[F] = ClientState(Map.empty)
}
