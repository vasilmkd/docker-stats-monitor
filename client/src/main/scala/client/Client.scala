package client

import cats.{ Applicative, CommutativeApplicative, MonadError }
import cats.data.StateT
import cats.effect.Sync
import cats.implicits._
import fs2.Pipe
import io.circe.generic.auto._
import io.circe.parser._
import org.scalajs.dom._

import chartist._, Chartist._
import models._

class Client[F[_]: Sync] {

  private type ClientState = Map[String, ContainerState]

  private object ClientState {
    val empty: ClientState = Map.empty
  }

  val run: Pipe[F, MessageEvent, Unit] =
    _.evalMapAccumulate(ClientState.empty)((state, e) => onMessage(state, e).map((_, ()))).void

  private val chartsElement: F[Element] = elementById("charts")

  private def appendChild(element: Element, child: Element): F[Unit] =
    Sync[F].delay(element.appendChild(child)) >> Sync[F].unit

  private def removeChild(element: Element, child: Element): F[Unit] =
    Sync[F].delay(element.removeChild(child)) >> Sync[F].unit

  private def elementById(id: String): F[Element] =
    Sync[F].delay(document.getElementById(id))

  implicit private def instance(
    implicit A: Applicative[StateT[F, ClientState, *]]
  ): CommutativeApplicative[StateT[F, ClientState, *]] =
    new CommutativeApplicative[StateT[F, ClientState, *]] {
      def ap[A, B](ff: StateT[F, ClientState, A => B])(fa: StateT[F, ClientState, A]): StateT[F, ClientState, B] =
        A.ap(ff)(fa)

      def pure[A](x: A): StateT[F, ClientState, A] =
        A.pure(x)
    }

  private def onMessage(state: ClientState, e: MessageEvent): F[ClientState] =
    for {
      stats <- decodeStats(e.data.toString)
      parts = partition(stats, state)
      newState <- (for {
                   _ <- parts.removed.unorderedTraverse(onRemoved)
                   _ <- parts.added.unorderedTraverse(id => onAdded(stats.data.find(_.id === id).get))
                   _ <- parts.updated.unorderedTraverse(id => onUpdated(stats.data.find(_.id === id).get))
                 } yield ()).run(state)
    } yield newState._1

  private def decodeStats(json: String): F[Stats] =
    MonadError[F, Throwable].fromEither(decode[Stats](json))

  private def partition(stats: Stats, state: Map[String, ContainerState]): Client.Partition = {
    val keySet   = state.keySet
    val statsIds = stats.data.map(_.id)

    val removed = keySet.diff(statsIds)
    val added   = statsIds.diff(keySet)
    val updated = keySet.intersect(statsIds)

    Client.Partition(removed, added, updated)
  }

  private def onRemoved(id: String): StateT[F, ClientState, Unit] =
    StateT { state =>
      for {
        charts <- chartsElement
        child  = state(id).element
        _      <- removeChild(charts, child)
      } yield (state - id, ())
    }

  private def onAdded(cd: ContainerData): StateT[F, ClientState, Unit] =
    StateT { state =>
      for {
        row <- rowElement
        _ <- List(
              nameCard(cd),
              cpuCard(cd),
              memCard(cd),
              ioCard(cd)
            ).traverse(_.flatMap(appendChild(row, _)))
        div            = document.createElement("div")
        _              <- appendChild(div, row)
        _              <- appendChild(div, document.createElement("hr"))
        charts         <- chartsElement
        _              <- appendChild(charts, div)
        (cpu, cpuData) = cpuChart(cd)
        (mem, memData) = memChart(cd)
      } yield ((state + (cd.id -> ContainerState(div, cpu, cpuData, mem, memData))), ())
    }

  private def onUpdated(cd: ContainerData): StateT[F, ClientState, Unit] =
    StateT { state =>
      val cs = state(cd.id)
      for {
        _ <- Sync[F].delay(cs.cpuData.add(cd.cpuPercentage))
        _ <- Sync[F].delay(cs.cpuChart.update(cs.cpuData))
        _ <- elementById(s"cpu-usage-${cd.id}").flatMap(e => Sync[F].delay(e.innerText = s"${cd.cpuPercentage}%"))
        _ <- Sync[F].delay(cs.memData.add(cd.memPercentage))
        _ <- Sync[F].delay(cs.memChart.update(cs.memData))
        _ <- elementById(s"mem-usage-${cd.id}").flatMap(e => Sync[F].delay(e.innerText = cd.memUsage))
        _ <- elementById(s"net-usage-${cd.id}").flatMap(e => Sync[F].delay(e.innerText = cd.netIO))
        _ <- elementById(s"block-usage-${cd.id}").flatMap(e => Sync[F].delay(e.innerText = cd.blockIO))
        _ <- elementById(s"pids-${cd.id}").flatMap(e => Sync[F].delay(e.innerText = cd.pids.toString))
      } yield (state, ())
    }

  private val rowElement: F[Element] =
    Sync[F].delay {
      val row = document.createElement("div")
      row.classList.add("mdc-layout-grid__inner")
      row
    }

  private def nameCard(cd: ContainerData): F[Element] =
    for {
      card <- cardElement(2)
      _ <- List(
            labelElement("Container name:"),
            textElement(cd.name),
            labelElement("Container id:"),
            textElement(cd.id.substring(0, 12))
          ).traverse(_.flatMap(appendChild(card, _)))
    } yield card

  private def cardElement(span: Int): F[Element] =
    Sync[F].delay {
      val card = document.createElement("div")
      card.classList.add(s"mdc-layout-grid__cell--span-$span")
      card.classList.add("mdc-card")
      card
    }

  private def labelElement(text: String): F[Element] =
    Sync[F].delay {
      val label = document.createElement("h5")
      label.classList.add("label")
      label.innerText = text
      label
    }

  private def textElement(text: String, id: Option[String] = None): F[Element] =
    Sync[F].delay {
      val p = document.createElement("p")
      p.innerText = text
      if (id.isDefined) p.id = id.get
      p
    }

  private def cpuCard(cd: ContainerData): F[Element] =
    for {
      card <- cardElement(4)
      _ <- List(
            labelElement("CPU usage:"),
            textElement(cd.cpuPercentage.toString, Some(s"cpu-usage-${cd.id}")),
            labelElement("CPU %"),
            chartElement(s"cpu-${cd.id}")
          ).traverse(_.flatMap(appendChild(card, _)))
    } yield card

  private def memCard(cd: ContainerData): F[Element] =
    for {
      card <- cardElement(4)
      _ <- List(
            labelElement("Memory usage:"),
            textElement(cd.cpuPercentage.toString, Some(s"mem-usage-${cd.id}")),
            labelElement("Memory %"),
            chartElement(s"mem-${cd.id}")
          ).traverse(_.flatMap(appendChild(card, _)))
    } yield card

  private def chartElement(id: String): F[Element] =
    Sync[F].delay {
      val chart = document.createElement("div")
      chart.classList.add("ct-chart")
      chart.id = id
      chart
    }

  private def ioCard(cd: ContainerData): F[Element] =
    for {
      card <- cardElement(2)
      _ <- List(
            labelElement("Network I/O:"),
            textElement(cd.netIO, Some(s"net-usage-${cd.id}")),
            labelElement("Block I/O:"),
            textElement(cd.blockIO, Some(s"block-usage-${cd.id}")),
            labelElement("PIDs:"),
            textElement(cd.pids.toString, Some(s"pids-${cd.id}"))
          ).traverse(_.flatMap(appendChild(card, _)))
    } yield card

  private def cpuChart(cd: ContainerData): (Line, Data) = {
    val data    = new Data(cd.cpuPercentage)
    val options = Options(low = 0, axisX = Axis(false, false))
    (new Line(s"#cpu-${cd.id}", data, options), data)
  }

  private def memChart(cd: ContainerData): (Line, Data) = {
    val data    = new Data(cd.memPercentage)
    val options = Options(low = 0, high = 100, axisX = Axis(false, false))
    (new Line(s"#mem-${cd.id}", data, options), data)
  }
}

private object Client {
  case class Partition(removed: Set[String], added: Set[String], updated: Set[String])
}
