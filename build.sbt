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
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % "0.21.3",
      "org.http4s" %% "http4s-dsl"          % "0.21.3",
      "org.http4s" %% "http4s-circe"        % "0.21.3",
      "io.circe"   %% "circe-generic"       % "0.13.0",
      "org.slf4j"  % "slf4j-simple"         % "1.7.30"
    ),
    (Universal / mappings) ++= directory("static"),
    (Universal / topLevelDirectory) := Some((ThisBuild / name).value),
    graalVMNativeImageOptions ++= Seq(
      "--verbose",
      "--no-server",
      "--no-fallback",
      "--static",
      "--enable-http",
      "--enable-https",
      "--enable-all-security-services",
      "--report-unsupported-elements-at-runtime",
      "--allow-incomplete-classpath",
      "-H:+ReportExceptionStackTraces",
      "-H:+ReportUnsupportedElementsAtRuntime",
      "-H:+TraceClassInitialization",
      "-H:+PrintClassInitialization",
      "-H:+StackTrace",
      "-H:+JNI",
      "-H:-SpawnIsolates",
      "-H:-UseServiceLoaderFeature",
      "-H:UseMuslC=../../../bundle/",
      "--initialize-at-build-time=scala.runtime.Statics$VM"
    )
  )
  .dependsOn(shared.jvm)

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    cleanFiles ++= Seq(
      (ThisBuild / baseDirectory).value / "static" / "js" / "client.js",
      (ThisBuild / baseDirectory).value / "static" / "js" / "client.js.map"
    ),
    (Compile / fastOptJS / artifactPath) := (ThisBuild / baseDirectory).value / "static" / "js" / "client.js",
    (Compile / fullOptJS / artifactPath) := (ThisBuild / baseDirectory).value / "static" / "js" / "client.js",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0",
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(shared.js)
