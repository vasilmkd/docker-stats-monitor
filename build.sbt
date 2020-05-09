import NativePackagerHelper._

ThisBuild / name := "docker-stats-monitor"
ThisBuild / organization := "mk.ukim.finki.inssok.stats.monitor"
ThisBuild / version := "0.0.2"
ThisBuild / scalaVersion := "2.13.2"

lazy val root = (project in file("."))
  .aggregate(shared.jvm, shared.js, server, client)
  .settings(
    (Compile / run) := Def
      .sequential(
        client / Compile / fastOptJS,
        (server / Compile / run).toTask("")
      )
      .value
  )

lazy val shared = crossProject(JVMPlatform, JSPlatform)
  .in(file("shared"))

lazy val server = (project in file("server"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s"     %% "http4s-blaze-server" % "0.21.3",
      "org.http4s"     %% "http4s-dsl"          % "0.21.3",
      "org.http4s"     %% "http4s-circe"        % "0.21.3",
      "io.circe"       %% "circe-generic"       % "0.13.0",
      "ch.qos.logback" % "logback-classic"      % "1.2.3"
    ),
    (Universal / mappings) ++= directory("static"),
    (Universal / topLevelDirectory) := Some((ThisBuild / name).value)
  )
  .dependsOn(shared.jvm)

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    cleanFiles ++= Seq(
      (ThisBuild / baseDirectory).value / "static" / "js" / "client-fastopt.js",
      (ThisBuild / baseDirectory).value / "static" / "js" / "client-fastopt.js.map"
    ),
    (Compile / fastOptJS / artifactPath) := (ThisBuild / baseDirectory).value / "static" / "js" / "client-fastopt.js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0",
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(shared.js)
