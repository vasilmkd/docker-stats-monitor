package model

final case class ContainerData(
  id: String,
  name: String,
  image: String,
  runningFor: String,
  status: String,
  cpuPercentage: Double,
  memUsage: String,
  memPercentage: Double,
  netIO: String,
  blockIO: String,
  pids: Int,
  size: String,
  ports: String
)
