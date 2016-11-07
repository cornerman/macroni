lazy val commons = Seq(
  scalaVersion := "2.11.8",
  scalacOptions ++=
    "-encoding" :: "UTF-8" ::
    "-unchecked" ::
    "-deprecation" ::
    "-explaintypes" ::
    "-feature" ::
    "-language:_" ::
    "-Xlint:_" ::
    "-Ywarn-unused" ::
    Nil,
    organization := "com.github.cornerman",
    version := "0.0.1-SNAPSHOT"
)

lazy val macros = project.in(file("macros"))
  .settings(commons: _*)
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    publish := {},
    publishLocal := {}
  )

lazy val core = project.in(file("core"))
  .dependsOn(macros % "compile-internal;test-internal")
  .settings(commons: _*)
  .settings(
    name := "macroni",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" % "test" cross CrossVersion.full),
    libraryDependencies ++=
      "org.scala-lang" % "scala-compiler" % scalaVersion.value ::
      "org.scala-lang" % "scala-reflect" % scalaVersion.value ::
      "org.specs2" %% "specs2-core" % "3.8.4" ::
      "org.specs2" %% "specs2-mock" % "3.8.4" ::
      Nil,
    // include the macro classes and resources in the main jar
    mappings in (Compile, packageBin) ++= mappings.in(macros, Compile, packageBin).value,
    // include the macro sources in the main source jar
    mappings in (Compile, packageSrc) ++= mappings.in(macros, Compile, packageSrc).value
  )
