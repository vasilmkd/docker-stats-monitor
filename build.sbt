ThisBuild / name := "docker-stats-monitor"
ThisBuild / organization := "mk.ukim.finki.inssok.stats.monitor"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.1"

lazy val root = (project in file("."))
  .aggregate(client, server, shared.jvm, shared.js)
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
  .jvmSettings(
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.1"
  )

lazy val server = (project in file("server"))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
    )
  )
  .dependsOn(shared.jvm)

lazy val deleteFiles = taskKey[Unit]("Delete generated Scala JS files")

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    cleanFiles ++= Seq(
      (server / baseDirectory).value / "public" / "javascripts" / "client-fastopt.js",
      (server / baseDirectory).value / "public" / "javascripts" / "client-fastopt.js.map"
    ),
    (Compile / fastOptJS / artifactPath) := (server / baseDirectory).value / "public" / "javascripts" / "client-fastopt.js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0",
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(shared.js)
