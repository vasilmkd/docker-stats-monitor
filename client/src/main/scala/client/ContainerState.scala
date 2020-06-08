package client

import org.scalajs.dom.Element

import client.chartist._
import client.chartist.Chartist.Line

final case class ContainerState(
  element: Element,
  cpuChart: Line,
  cpuData: Data,
  memChart: Line,
  memData: Data
)
