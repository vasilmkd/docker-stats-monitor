package client

import scala.collection.mutable
import scala.scalajs.js
import org.scalajs.dom._

import client.chartist._, Chartist._
import models._

object Main {

  private val state: mutable.Map[String, ContainerState] = mutable.Map.empty

  def main(args: Array[String]): Unit = {
    val socket = new WebSocket("ws://localhost:9000/ws")

    val charts = document.getElementById("charts")

    socket.onmessage = (e: MessageEvent) => {
      val json  = js.JSON.parse(e.data.toString)
      val stats = Stats(json)
      println(stats)

      val keySet   = state.keySet
      val statsIds = stats.data.map(_.id)

      val removed = keySet.diff(statsIds)
      val added   = statsIds.diff(keySet)
      val updated = keySet.intersect(statsIds)

      removed.foreach { id =>
        charts.removeChild(state(id).element)
        state.remove(id)
      }

      added.foreach { id =>
        val cd = stats.data.find(_.id == id).get

        val row = document.createElement("div")
        row.classList.add("mdc-layout-grid__inner")

        val nameCard = document.createElement("div")
        nameCard.classList.add("mdc-layout-grid__cell--span-2")
        nameCard.classList.add("mdc-card")

        val nameLabel = document.createElement("h5")
        nameLabel.classList.add("label")
        nameLabel.innerText = "Container name:"
        nameCard.appendChild(nameLabel)

        val name = document.createElement("p")
        name.innerText = cd.name
        nameCard.appendChild(name)

        val idLabel = document.createElement("h5")
        idLabel.classList.add("label")
        idLabel.innerText = "Container id:"
        nameCard.appendChild(idLabel)

        val idp = document.createElement("p")
        idp.innerText = cd.id.substring(0, 12)
        nameCard.appendChild(idp)

        row.appendChild(nameCard)

        val cpuCard = document.createElement("div")
        cpuCard.classList.add("mdc-layout-grid__cell")
        cpuCard.classList.add("mdc-card")
        val cpuTitle = document.createElement("h4")
        cpuTitle.innerText = "CPU %"
        cpuCard.appendChild(cpuTitle)
        val cpuChart = document.createElement("div")
        cpuChart.classList.add("ct-chart")
        cpuChart.id = s"cpu-$id"
        cpuCard.appendChild(cpuChart)
        row.appendChild(cpuCard)

        val memCard = document.createElement("div")
        memCard.classList.add("mdc-layout-grid__cell")
        memCard.classList.add("mdc-card")
        val memTitle = document.createElement("h4")
        memTitle.innerText = "Memory %"
        memCard.appendChild(memTitle)
        val memChart = document.createElement("div")
        memChart.classList.add("ct-chart")
        memChart.id = s"mem-$id"
        memCard.appendChild(memChart)
        row.appendChild(memCard)

        charts.appendChild(row)

        val cpuData    = new Data(cd.cpuPercentage)
        val cpuOptions = Options(low = 0, axisX = Axis(false, false))
        val cpu        = new Line(s"#cpu-$id", cpuData, cpuOptions)

        val memData    = new Data(cd.memPercentage)
        val memOptions = Options(low = 0, high = 100, axisX = Axis(false, false))
        val mem        = new Line(s"#mem-$id", memData, memOptions)

        state.put(id, ContainerState(row, cpu, cpuData, mem, memData))
      }

      updated.foreach { id =>
        val cd = stats.data.find(_.id == id).get
        val cs = state(id)
        cs.cpuData.add(cd.cpuPercentage)
        cs.cpuChart.update(cs.cpuData)
        cs.memData.add(cd.memPercentage)
        cs.memChart.update(cs.memData)
      }
    }
  }
}
