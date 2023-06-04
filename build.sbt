import ReleaseTransformations._

lazy val V = _root_.scalafix.sbt.BuildInfo

lazy val commonSettings = Def.settings(
  organization := "org.scalaz",
  homepage := Some(url("https://github.com/scalaz/scalazfix")),
  licenses := Seq("MIT License" -> url("https://opensource.org/licenses/mit-license")),
  description := "scalafix rule for scalaz",
  scalaVersion := V.scala212,
  addCompilerPlugin(scalafixSemanticdb),
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommandAndRemaining("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),
  pomExtra := (
    <developers>
        <developer>
          <id>xuwei-k</id>
          <name>Kenji Yoshida</name>
          <url>https://github.com/xuwei-k</url>
        </developer>
      </developers>
      <scm>
        <url>git@github.com:scalaz/scalazfix.git</url>
        <connection>scm:git:git@github.com:scalaz/scalazfix.git</connection>
      </scm>
  ),
  publishTo := sonatypePublishToBundle.value,
  Compile / doc / scalacOptions ++= {
    val hash = sys.process.Process("git rev-parse HEAD").lineStream_!.head
    Seq(
      "-sourcepath",
      (LocalRootProject / baseDirectory).value.getAbsolutePath,
      "-doc-source-url",
      s"https://github.com/scalaz/scalazfix/tree/${hash}€{FILE_PATH}.scala"
    )
  },
  scalacOptions ++= PartialFunction
    .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
      case Some((2, v)) if v >= 12 =>
        Seq(
          "-Ywarn-unused:imports",
        )
    }
    .toList
    .flatten,
  scalacOptions ++= PartialFunction
    .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
      case Some((2, v)) if v <= 12 =>
        Seq(
          "-Yno-adapted-args",
          "-Xfuture",
        )
    }
    .toList
    .flatten,
  scalacOptions ++= List(
    "-deprecation",
    "-unchecked",
    "-Yrangepos",
    "-P:semanticdb:synthetics:on"
  )
)

commonSettings
publish / skip := true

lazy val rules = project.settings(
  commonSettings,
  name := "scalazfix",
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
)

lazy val input = project.settings(
  commonSettings,
  libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.30", // scala-steward:off
  publish / skip := true
)

lazy val output = project.settings(
  commonSettings,
  libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.7",
  publish / skip := true
)

lazy val tests = project
  .settings(
    commonSettings,
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    Compile / compile :=
      (Compile / compile).dependsOn(compile.in(input, Compile)).value,
    scalafixTestkitOutputSourceDirectories :=
      sourceDirectories.in(output, Compile).value,
    scalafixTestkitInputSourceDirectories :=
      sourceDirectories.in(input, Compile).value,
    scalafixTestkitInputClasspath :=
      fullClasspath.in(input, Compile).value,
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
