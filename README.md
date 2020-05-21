# Docker stats monitor

[Http4s](https://http4s.org) application that uses [fs2](https://fs2.io) streams to send `docker stats` and `docker ps` data over a websocket connection to a Scala JS frontend which visualizes the results using the [chartist.js](https://gionkunz.github.io/chartist-js/) library.

## Docker image
https://hub.docker.com/r/vasilvasilev97/docker-stats-monitor

Slim docker image (`FROM scratch`) containing only the application and docker binaries, along with the static site data. The application binary is completely statically compiled and linked Linux binary with absolutely no dependencies, generated using [GraalVM](https://www.graalvm.org) `native-image`.

Run with:

```
docker run -d --name monitor -p "8080:8080" -v /var/run/docker.sock:/var/run/docker.sock vasilvasilev97/docker-stats-monitor
```

and visit [localhost:8080](http://localhost:8080/).

## Tests

There are integration tests with Docker that can be run using `sbt run`. Note: the docker executable needs to be available on the `PATH` and the docker daemon needs to be running when executing the tests.
