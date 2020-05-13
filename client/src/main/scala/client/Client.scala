package client

import cats.MonadError
import cats.data.StateT
import cats.effect.{ ConcurrentEffect, IO, Sync }
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Queue
import io.circe.generic.auto._
import io.circe.parser._
import org.scalajs.dom._

import chartist._, Chartist._
import models._

class Client[F[_]: ConcurrentEffect] {

  private type ClientState = Map[String, ContainerState]

  val run: F[Unit] =
    Stream
      .eval {
        for {
          ref   <- Ref[F].of[ClientState](Map.empty)
          queue <- Queue.unbounded[F, MessageEvent]
          _     <- socket(queue)
        } yield (ref, queue)
      }
      .flatMap {
        case (ref, queue) =>
          queue.dequeue.evalMap(onMessage(_, ref))
      }
      .compile
      .drain

  private def socket(queue: Queue[F, MessageEvent]): F[WebSocket] =
    for {
      ws <- Sync[F].delay(new WebSocket("ws://localhost:8080/ws"))
      _ <- Sync[F].delay {
            ws.onmessage = e => ConcurrentEffect[F].runAsync(queue.enqueue1(e))(_ => IO.unit).unsafeRunSync()
          }
    } yield ws

  private val chartsElement: F[Element] = elementById("charts")

  private def appendChild(element: Element, child: Element): F[Unit] =
    Sync[F].delay(element.appendChild(child)) >> Sync[F].unit

  private def removeChild(element: Element, child: Element): F[Unit] =
    Sync[F].delay(element.removeChild(child)) >> Sync[F].unit

  private def elementById(id: String): F[Element] =
    Sync[F].delay(document.getElementById(id))

  private def onMessage(e: MessageEvent, ref: Ref[F, ClientState]): F[Unit] =
    for {
      state    <- ref.get
      stats    <- decodeStats(e.data.toString)
      parts    = partition(stats, state)
      remState <- parts.removed.traverse(onRemoved(_).get).run(state)
      addState <- parts.added.traverse(id => onAdded(stats.data.find(_.id === id).get)).run(remState._1)
      _        <- parts.updated.traverse_(id => onUpdated(stats.data.find(_.id === id).get, addState._1))
      _        <- ref.set(addState._1)
    } yield ()

  private def decodeStats(json: String): F[Stats] =
    MonadError[F, Throwable].fromEither(decode[Stats](json))

  private def partition(stats: Stats, state: Map[String, ContainerState]): Client.Partition = {
    val keySet   = state.keySet
    val statsIds = stats.data.map(_.id)

    val removed = keySet.diff(statsIds)
    val added   = statsIds.diff(keySet)
    val updated = keySet.intersect(statsIds)

    Client.Partition(removed.toList, added.toList, updated.toList)
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

  private def onUpdated(cd: ContainerData, state: ClientState): F[Unit] = {
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
    } yield ()
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
  case class Partition(removed: List[String], added: List[String], updated: List[String])
}
