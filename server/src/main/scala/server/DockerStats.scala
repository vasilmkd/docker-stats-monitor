package server

import java.io.InputStream

import cats.effect.Sync

object DockerStats {

  def input[F[_]: Sync]: F[InputStream] =
    Sync[F].delay(statsBuilder.start().getInputStream())

  private val statsBuilder = new ProcessBuilder()
    .command(
      "docker",
      "stats",
      "--format",
      "table {{.ID}},{{.Name}},{{.CPUPerc}},{{.MemUsage}},{{.MemPerc}},{{.NetIO}},{{.BlockIO}},{{.PIDs}}",
      "--no-stream"
    )
}
