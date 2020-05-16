package client

import cats.{ Applicative, CommutativeApplicative }
import cats.data.StateT
import cats.effect.Sync
import cats.implicits._
import fs2.Pipe

import chartist._
import model._

class Client[F[_]: Sync: DOM: Charting] {

  val run: Pipe[F, DockerData, ClientState[F]] =
    _.evalMapAccumulate(ClientState.empty[F])(onData).map(_._1)

  implicit private def instance(
    implicit A: Applicative[StateT[F, ClientState[F], *]]
  ): CommutativeApplicative[StateT[F, ClientState[F], *]] =
    new CommutativeApplicative[StateT[F, ClientState[F], *]] {
      def ap[A, B](
        ff: StateT[F, ClientState[F], A => B]
      )(fa: StateT[F, ClientState[F], A]): StateT[F, ClientState[F], B] =
        A.ap(ff)(fa)

      def pure[A](x: A): StateT[F, ClientState[F], A] =
        A.pure(x)
    }

  private def onData(state: ClientState[F], data: DockerData): F[(ClientState[F], Unit)] = {
    val parts = state.partition(data)
    for {
      newState <- (for {
                   _ <- parts.removed.unorderedTraverse(onRemoved)
                   _ <- parts.added.unorderedTraverse(id => onAdded(data.find(_.id === id).get))
                   _ <- parts.updated.unorderedTraverse(id => onUpdated(data.find(_.id === id).get))
                 } yield ()).run(state)
    } yield newState
  }

  private def onRemoved(id: String): StateT[F, ClientState[F], Unit] =
    StateT { state =>
      for {
        _ <- DOM[F].onRemoved(id)
      } yield (state - id, ())
    }

  private def onAdded(cd: ContainerData): StateT[F, ClientState[F], Unit] =
    StateT { state =>
      for {
        _ <- DOM[F].onAdded(cd)
        cpuChart <- Charting[F].createLineChart(
                     s"#cpu-${cd.id}",
                     cd.cpuPercentage,
                     Options(low = 0, axisX = Axis(false, false))
                   )
        memChart <- Charting[F].createLineChart(
                     s"#mem-${cd.id}",
                     cd.memPercentage,
                     Options(low = 0, high = 100, axisX = Axis(false, false))
                   )
      } yield (state + (cd.id -> ChartState(cpuChart, memChart)), ())
    }

  private def onUpdated(cd: ContainerData): StateT[F, ClientState[F], Unit] =
    StateT { state =>
      for {
        cs <- state.get(cd.id)
        _  <- cs.cpuChart.update(cd.cpuPercentage)
        _  <- cs.memChart.update(cd.memPercentage)
        _  <- DOM[F].onUpdated(cd)
      } yield (state, ())
    }
}
