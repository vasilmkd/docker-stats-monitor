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
        val cpuUsageTitle = document.createElement("h5")
        cpuUsageTitle.classList.add("label")
        cpuUsageTitle.innerText = "CPU usage:"
        cpuCard.appendChild(cpuUsageTitle)
        val cpuUsage = document.createElement("p")
        cpuUsage.id = s"cpu-usage-$id"
        cpuUsage.innerText = s"${cd.cpuPercentage}%"
        cpuCard.appendChild(cpuUsage)
        val cpuTitle = document.createElement("h5")
        cpuTitle.innerText = "CPU %"
        cpuCard.appendChild(cpuTitle)
        val cpuChart = document.createElement("div")
        cpuChart.classList.add("ct-chart")
        cpuChart.id = s"cpu-$id"
        cpuCard.appendChild(cpuChart)
        row.appendChild(cpuCard)

        charts.appendChild(row)

        val memCard = document.createElement("div")
        memCard.classList.add("mdc-layout-grid__cell")
        memCard.classList.add("mdc-card")
        val memUsageTitle = document.createElement("h5")
        memUsageTitle.classList.add("label")
        memUsageTitle.innerText = "Memory usage:"
        memCard.appendChild(memUsageTitle)
        val memUsage = document.createElement("p")
        memUsage.id = s"mem-usage-$id"
        memUsage.innerText = cd.memUsage
        memCard.appendChild(memUsage)
        val memTitle = document.createElement("h5")
        memTitle.innerText = "Memory %"
        memCard.appendChild(memTitle)
        val memChart = document.createElement("div")
        memChart.classList.add("ct-chart")
        memChart.id = s"mem-$id"
        memCard.appendChild(memChart)
        row.appendChild(memCard)

        val ioCard = document.createElement("div")
        ioCard.classList.add("mdc-layout-grid__cell--span-2")
        ioCard.classList.add("mdc-card")
        val netUsageTitle = document.createElement("h5")
        netUsageTitle.classList.add("label")
        netUsageTitle.innerText = "Network I/O:"
        ioCard.appendChild(netUsageTitle)
        val netUsage = document.createElement("p")
        netUsage.id = s"net-usage-$id"
        netUsage.innerText = cd.netIO
        ioCard.appendChild(netUsage)
        val blockUsageTitle = document.createElement("h5")
        blockUsageTitle.classList.add("label")
        blockUsageTitle.innerText = "Block I/O:"
        ioCard.appendChild(blockUsageTitle)
        val blockUsage = document.createElement("p")
        blockUsage.id = s"block-usage-$id"
        blockUsage.innerText = cd.blockIO
        ioCard.appendChild(blockUsage)
        val pidsTitle = document.createElement("h5")
        pidsTitle.classList.add("label")
        pidsTitle.innerText = "PIDs"
        ioCard.appendChild(pidsTitle)
        val pids = document.createElement("p")
        pids.id = s"pids-$id"
        pids.innerText = cd.pids.toString
        ioCard.appendChild(pids)
        row.appendChild(ioCard)

        val div = document.createElement("div")
        div.appendChild(row)
        div.appendChild(document.createElement("hr"))

        charts.appendChild(div)

        val cpuData    = new Data(cd.cpuPercentage)
        val cpuOptions = Options(low = 0, axisX = Axis(false, false))
        val cpu        = new Line(s"#cpu-$id", cpuData, cpuOptions)

        val memData    = new Data(cd.memPercentage)
        val memOptions = Options(low = 0, high = 100, axisX = Axis(false, false))
        val mem        = new Line(s"#mem-$id", memData, memOptions)

        state.put(id, ContainerState(div, cpu, cpuData, mem, memData))
      }

      updated.foreach { id =>
        val cd = stats.data.find(_.id == id).get
        val cs = state(id)
        cs.cpuData.add(cd.cpuPercentage)
        cs.cpuChart.update(cs.cpuData)
        document.getElementById(s"cpu-usage-$id").innerText = s"${cd.cpuPercentage}%"
        cs.memData.add(cd.memPercentage)
        cs.memChart.update(cs.memData)
        document.getElementById(s"mem-usage-$id").innerText = cd.memUsage
        document.getElementById(s"net-usage-$id").innerText = cd.netIO
        document.getElementById(s"block-usage-$id").innerText = cd.blockIO
        document.getElementById(s"pids-$id").innerText = cd.pids.toString
      }
    }
  }
}
