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

  override def onAdded(cd: ContainerData): F[Unit] = {
    val running = cd.status.startsWith("Up")
    val card    = document.createElement("div")
    for {
      _        <- Sync[F].delay(card.classList.add("mdc-card"))
      nameRow  <- rowElement
      _        <- List(
                    nameCardLabelElement(2, "Container name:", cd.name),
                    nameCardLabelElement(2, "Container id:", cd.id),
                    nameCardLabelElement(2, "Container image:", cd.image),
                    nameCardLabelElement(2, "Created:", cd.runningFor, Some(s"running-${cd.id}")),
                    nameCardLabelElement(2, "Status:", cd.status, Some(s"status-${cd.id}")),
                    nameCardToggle(cd.id, running)
                  ).traverse_(_.flatMap(appendChild(nameRow, _)))
      chartRow <- chartRowElement(cd.id, running)
      _        <- List(
                    cpuCard(cd),
                    memCard(cd),
                    ioCard(cd)
                  ).traverse_(_.flatMap(appendChild(chartRow, _)))
      _        <- appendChild(card, nameRow)
      _        <- appendChild(card, chartRow)
      div       = document.createElement("div")
      _        <- Sync[F].delay(div.id = s"row-${cd.id}")
      _        <- appendChild(div, card)
      _        <- appendChild(div, document.createElement("hr"))
      charts   <- chartsElement
      _        <- appendChild(charts, div)
    } yield ()
  }

  override def onUpdated(cd: ContainerData): F[Unit] =
    for {
      _ <- updateText(s"running-${cd.id}", cd.runningFor)
      _ <- updateText(s"status-${cd.id}", cd.status)
      _ <- updateText(s"cpu-usage-${cd.id}", s"${cd.cpuPercentage}%")
      _ <- updateText(s"mem-usage-${cd.id}", cd.memUsage)
      _ <- updateText(s"net-usage-${cd.id}", cd.netIO)
      _ <- updateText(s"block-usage-${cd.id}", cd.blockIO)
      _ <- updateText(s"pids-${cd.id}", cd.pids.show)
      _ <- updateText(s"size-${cd.id}", cd.size)
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

  private def chartRowElement(id: String, running: Boolean): F[Element] =
    for {
      row <- rowElement
      _   <- Sync[F].delay(row.id = s"chart-row-$id")
      _   <- Sync[F].delay(row.classList.add("hidden")).whenA(!running)
    } yield row

  private def cpuCard(cd: ContainerData): F[Element] =
    for {
      card <- cardElement(5)
      _    <- List(
                labelElement("CPU usage:"),
                textElement(s"${cd.cpuPercentage}%", Some(s"cpu-usage-${cd.id}")),
                labelElement("CPU %"),
                chartElement(s"cpu-${cd.id}")
              ).traverse_(_.flatMap(appendChild(card, _)))
    } yield card

  private def memCard(cd: ContainerData): F[Element] =
    for {
      card <- cardElement(5)
      _    <- List(
                labelElement("Memory usage:"),
                textElement(s"${cd.memUsage}", Some(s"mem-usage-${cd.id}")),
                labelElement("Memory %"),
                chartElement(s"mem-${cd.id}")
              ).traverse_(_.flatMap(appendChild(card, _)))
    } yield card

  private def ioCard(cd: ContainerData): F[Element] =
    for {
      card <- cardElement(2)
      _    <- List(
                labelElement("Network I/O:"),
                textElement(cd.netIO, Some(s"net-usage-${cd.id}")),
                labelElement("Block I/O:"),
                textElement(cd.blockIO, Some(s"block-usage-${cd.id}")),
                labelElement("PIDs:"),
                textElement(cd.pids.show, Some(s"pids-${cd.id}")),
                labelElement("Size:"),
                textElement(cd.size, Some(s"size-${cd.id}")),
                labelElement("Ports:"),
                textElement(cd.ports, Some(s"ports-${cd.id}"))
              ).traverse_(_.flatMap(appendChild(card, _)))
    } yield card

  private def cardElement(span: Int): F[Element] =
    Sync[F].delay {
      val card = document.createElement("div")
      card.classList.add(s"mdc-layout-grid__cell--span-$span")
      card
    }

  private def nameCardLabelElement(span: Int, label: String, text: String, id: Option[String] = None): F[Element] =
    for {
      div <- Sync[F].delay {
               val div = document.createElement("div")
               div.classList.add(s"mdc-layout-grid__cell--span-$span")
               div
             }
      _   <- List(
               labelElement(label),
               textElement(text, id)
             ).traverse_(_.flatMap(appendChild(div, _)))
    } yield div

  private def nameCardToggle(id: String, running: Boolean): F[Element] =
    Sync[F].delay {
      val div    = document.createElement("div")
      div.classList.add("mdc-layout-grid__cell--span-2")
      div.classList.add("button-div")
      val button = document.createElement("button")
      button.setAttribute("type", "button")
      button.classList.add("mdc-button")
      button.classList.add("mdc-button--raised")
      val span   = document.createElement("span")
      span.classList.add("mdc-button__label")
      span.innerText = if (running) DOMImpl.hideStats else DOMImpl.showStats
      button.addEventListener(
        "click",
        (_: Event) => {
          val chartRow = document.getElementById(s"chart-row-$id")
          if (span.innerText === DOMImpl.showStats) {
            chartRow.classList.remove("hidden")
            span.innerText = DOMImpl.hideStats
          } else {
            chartRow.classList.add("hidden")
            span.innerText = DOMImpl.showStats
          }
        }
      )
      button.appendChild(span)
      div.appendChild(button)
      div
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
      chart.id = id
      chart
    }
}

object DOMImpl {
  def apply[F[_]: Sync]: DOM[F] = new DOMImpl[F]

  private[impl] val showStats: String = "SHOW STATS"
  private[impl] val hideStats: String = "HIDE STATS"
}
