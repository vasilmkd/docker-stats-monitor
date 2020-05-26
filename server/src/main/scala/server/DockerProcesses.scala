package server

import java.io.InputStream

import cats.effect.Sync

object DockerProcesses {

  def input[F[_]: Sync]: F[InputStream] =
    Sync[F].delay(psBuilder.start().getInputStream())

  private val psBuilder = new ProcessBuilder()
    .command(
      "docker",
      "ps",
      "-a",
      "--format",
      "table {{.ID}},,,{{.Image}},,,{{.RunningFor}},,,{{.Ports}},,,{{.Status}},,,{{.Size}}"
    )
}
