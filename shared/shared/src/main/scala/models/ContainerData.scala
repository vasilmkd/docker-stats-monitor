package models

final case class ContainerData(id: String, name: String, cpuPercentage: Double, memPercentage: Double)

object ContainerData extends ContainerDataPlatformSpecific
