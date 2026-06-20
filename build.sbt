import ReleaseTransformations._

lazy val V = _root_.scalafix.sbt.BuildInfo

val scalaVersions = Seq(V.scala213, V.scala212)

lazy val commonSettings = Def.settings(
  organization := "org.scalaz",
  homepage := Some(url("https://github.com/scalaz/scalazfix")),
  licenses := Seq("MIT License" -> url("https://opensource.org/licenses/mit-license")),
  description := "scalafix rule for scalaz",
  addCompilerPlugin(scalafixSemanticdb),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("publishSigned"),
    releaseStepCommandAndRemaining("sonaRelease"),
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
  publishTo := (if (isSnapshot.value) None else localStaging.value),
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

lazy val rules = projectMatrix
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions)
  .settings(
    commonSettings,
    name := "scalazfix",
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
  )

lazy val input = projectMatrix
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions)
  .settings(
    commonSettings,
    libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.36", // scala-steward:off
    publish / skip := true
  )

lazy val output = projectMatrix
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions)
  .settings(
    commonSettings,
    libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.9",
    publish / skip := true
  )

lazy val tests = projectMatrix
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions)
  .settings(
    commonSettings,
    publish / skip := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    Compile / compile :=
      (Compile / compile).dependsOn(Def.taskDyn { input.jvm(scalaVersion.value) / Compile / compile }).value,
    scalafixTestkitOutputSourceDirectories :=
      Def.taskDyn {
        val p = output.jvm(scalaVersion.value)
        Def.task((p / Compile / sourceDirectories).value)
      }.value,
    scalafixTestkitInputSourceDirectories :=
      Def.taskDyn {
        val p = input.jvm(scalaVersion.value)
        Def.task((p / Compile / sourceDirectories).value)
      }.value,
    scalafixTestkitInputClasspath :=
      Def.taskDyn {
        val p = input.jvm(scalaVersion.value)
        Def.task((p / Compile / fullClasspath).value)
      }.value,
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
