package client.impl

import cats.effect.Sync
import cats.implicits._
import org.scalajs.dom._

import client.DOM
import model._

class DOMImpl[F[_]: Sync] extends DOM[F] {

  override def onRemoved(id: String): F[Unit] =
    for {
      charts <- chartsElement
      child  <- elementById(s"row-$id")
      _      <- removeChild(charts, child)
    } yield ()

  override def onAdded(cd: ContainerData): F[Unit] =
    for {
      row <- rowElement
      _ <- List(
            nameCard(cd),
            cpuCard(cd),
            memCard(cd),
            ioCard(cd)
          ).traverse(_.flatMap(appendChild(row, _)))
      div    = document.createElement("div")
      _      <- Sync[F].delay(div.id = s"row-${cd.id}")
      _      <- appendChild(div, row)
      _      <- appendChild(div, document.createElement("hr"))
      charts <- chartsElement
      _      <- appendChild(charts, div)
    } yield ()

  override def onUpdated(cd: ContainerData): F[Unit] =
    for {
      _ <- updateText(s"running-${cd.id}", cd.runningFor)
      _ <- updateText(s"status-${cd.id}", cd.status)
      _ <- updateText(s"cpu-usage-${cd.id}", s"${cd.cpuPercentage}%")
      _ <- updateText(s"mem-usage-${cd.id}", cd.memUsage)
      _ <- updateText(s"net-usage-${cd.id}", cd.netIO)
      _ <- updateText(s"block-usage-${cd.id}", cd.blockIO)
      _ <- updateText(s"pids-${cd.id}", cd.pids.toString)
      _ <- updateText(s"size-${cd.id}", cd.size)
      _ <- updateText(s"ports-${cd.id}", cd.ports)
    } yield ()

  private def updateText(id: String, text: String): F[Unit] =
    for {
      e <- elementById(id)
      _ <- Sync[F].delay(e.innerText = text)
    } yield ()

  private val chartsElement: F[Element] = elementById("charts")

  private def elementById(id: String): F[Element] =
    Sync[F].delay(document.getElementById(id))

  private def appendChild(element: Element, child: Element): F[Unit] =
    Sync[F].delay(element.appendChild(child)) >> Sync[F].unit

  private def removeChild(element: Element, child: Element): F[Unit] =
    Sync[F].delay(element.removeChild(child)) >> Sync[F].unit

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
            textElement(cd.id.substring(0, 12)),
            labelElement("Container image:"),
            textElement(cd.image),
            labelElement("Running for:"),
            textElement(cd.runningFor, Some(s"running-${cd.id}")),
            labelElement("Status:"),
            textElement(cd.status, Some(s"status-${cd.id}"))
          ).traverse(_.flatMap(appendChild(card, _)))
    } yield card

  private def cpuCard(cd: ContainerData): F[Element] =
    for {
      card <- cardElement(4)
      _ <- List(
            labelElement("CPU usage:"),
            textElement(s"${cd.cpuPercentage}%", Some(s"cpu-usage-${cd.id}")),
            labelElement("CPU %"),
            chartElement(s"cpu-${cd.id}")
          ).traverse(_.flatMap(appendChild(card, _)))
    } yield card

  private def memCard(cd: ContainerData): F[Element] =
    for {
      card <- cardElement(4)
      _ <- List(
            labelElement("Memory usage:"),
            textElement(s"${cd.memPercentage}%", Some(s"mem-usage-${cd.id}")),
            labelElement("Memory %"),
            chartElement(s"mem-${cd.id}")
          ).traverse(_.flatMap(appendChild(card, _)))
    } yield card

  private def ioCard(cd: ContainerData): F[Element] =
    for {
      card <- cardElement(2)
      _ <- List(
            labelElement("Network I/O:"),
            textElement(cd.netIO, Some(s"net-usage-${cd.id}")),
            labelElement("Block I/O:"),
            textElement(cd.blockIO, Some(s"block-usage-${cd.id}")),
            labelElement("PIDs:"),
            textElement(cd.pids.toString, Some(s"pids-${cd.id}")),
            labelElement("Size:"),
            textElement(cd.size, Some(s"size-${cd.id}")),
            labelElement("Ports:"),
            textElement(cd.ports, Some(s"ports-${cd.id}"))
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

  private def chartElement(id: String): F[Element] =
    Sync[F].delay {
      val chart = document.createElement("div")
      chart.classList.add("ct-chart")
      chart.classList.add("ct-minor-sixth")
      chart.id = id
      chart
    }
}

object DOMImpl {
  def apply[F[_]: Sync]: DOM[F] = new DOMImpl[F]
}
