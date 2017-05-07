version in ThisBuild := "0.1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.11"

scalacOptions in ThisBuild ++=
  "-encoding" :: "UTF-8" ::
  "-unchecked" ::
  "-deprecation" ::
  "-explaintypes" ::
  "-feature" ::
  "-language:_" ::
  "-Xlint:_" ::
  "-Ywarn-unused" ::
  Nil

lazy val macros = project.in(file("macros"))
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    publishArtifact := false
  )

lazy val core = project.in(file("core"))
  .dependsOn(macros % "compile-internal;test-internal")
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

organization in Global := "com.github.cornerman"

pgpSecretRing in Global := file("secring.gpg")
pgpPublicRing in Global := file("pubring.gpg")
pgpPassphrase in Global := Some("".toCharArray)

pomExtra := {
  <url>https://github.com/cornerman/macroni</url>
  <licenses>
    <license>
      <name>The MIT license</name>
      <url>http://www.opensource.org/licenses/mit-license.php</url>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/cornerman/macroni</url>
    <connection>scm:git:git@github.com:cornerman/macroni.git</connection>
  </scm>
  <developers>
    <developer>
      <id>jkaroff</id>
      <name>Johannes Karoff</name>
      <url>https://github.com/cornerman</url>
    </developer>
  </developers>
}
