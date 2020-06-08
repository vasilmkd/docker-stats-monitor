# Docker stats monitor

Play Framework application that uses Akka streams to send `docker stats` data over a websocket connection to a Scala JS frontend which visualizes the results using the [chartist.js](https://gionkunz.github.io/chartist-js/) library.

The `build.sbt` build description contains useful task definition and sequencing code. I expect to evolve the project with more functionality and better user interface. I also plan to create a Docker image for quick deployment as part of the developer workflow.

Run with:

```
docker run -d --name monitor -p "9000:9000" -v /var/run/docker.sock:/var/run/docker.sock vasilvasilev97/docker-stats-monitor
```
