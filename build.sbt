ThisBuild / name := "docker-stats-monitor"
ThisBuild / version := "0.3.3"
ThisBuild / scalaVersion := "2.13.2"

val compilerOptions = Seq(
  "-deprecation",                  // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8",                         // Specify character encoding used by source files.
  "-explaintypes",                 // Explain type errors in more detail.
  "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds",         // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",              // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
  "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",       // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",        // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",              // Warn when dead code is identified.
  "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",          // Warn when numerics are widened.
  "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",          // Warn if a local definition is unused.
  "-Ywarn-unused:params",          // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",        // Warn if a private member is unused.
  "-Ywarn-value-discard"           // Warn when non-Unit expression results are unused.
)

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
  .settings(scalacOptions ++= compilerOptions)
  .in(file("shared"))

lazy val server = (project in file("server"))
  .enablePlugins(GraalVMNativeImagePlugin)
  .settings(
    scalacOptions ++= compilerOptions,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "org.http4s"     %% "http4s-blaze-server" % "0.21.4",
      "org.http4s"     %% "http4s-dsl"          % "0.21.4",
      "org.http4s"     %% "http4s-circe"        % "0.21.4",
      "io.circe"       %% "circe-generic"       % "0.13.0",
      "org.slf4j"       % "slf4j-simple"        % "1.7.30",
      "org.scalameta" %%% "munit"               % "0.7.7" % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
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
      "-H:+RemoveSaturatedTypeFlows",
      "-H:+StackTrace",
      "-H:+JNI",
      "-H:-SpawnIsolates",
      "-H:-UseServiceLoaderFeature",
      "-H:UseMuslC=../../../bundle/",
      "--install-exit-handlers",
      "--initialize-at-build-time=scala.runtime.Statics$VM"
    )
  )
  .dependsOn(shared.jvm)

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalacOptions ++= compilerOptions.filterNot(_ == "-Ywarn-unused:params"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    cleanFiles ++= Seq(
      (ThisBuild / baseDirectory).value / "static" / "js" / "client.js",
      (ThisBuild / baseDirectory).value / "static" / "js" / "client.js.map"
    ),
    (Compile / fastOptJS / artifactPath) := (ThisBuild / baseDirectory).value / "static" / "js" / "client.js",
    (Compile / fullOptJS / artifactPath) := (ThisBuild / baseDirectory).value / "static" / "js" / "client.js",
    libraryDependencies ++= Seq(
      "org.scala-js"  %%% "scalajs-dom"   % "1.0.0",
      "org.typelevel" %%% "cats-effect"   % "2.1.3",
      "co.fs2"        %%% "fs2-core"      % "2.3.0",
      "io.circe"      %%% "circe-generic" % "0.13.0",
      "io.circe"      %%% "circe-parser"  % "0.13.0",
      "org.scalameta" %%% "munit"         % "0.7.7" % Test
    ),
    scalaJSUseMainModuleInitializer := true,
    testFrameworks += new TestFramework("munit.Framework"),
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
  )
  .dependsOn(shared.js)
