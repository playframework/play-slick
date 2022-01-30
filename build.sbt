import scala.sys.process._

import com.typesafe.tools.mima.plugin.MimaPlugin._
import interplay.ScalaVersions._

ThisBuild / resolvers += Resolver.sonatypeRepo("releases")

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
ThisBuild / dynverVTagPrefix := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  val v = version.value
  if (dynverGitDescribeOutput.value.hasNoTags)
    throw new MessageOnlyException(
      s"Failed to derive version from git tags. Maybe run `git fetch --unshallow`? Version: $v"
    )
  s
}

lazy val commonSettings = Seq(
  // Work around https://issues.scala-lang.org/browse/SI-9311
  scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings")),
  scalaVersion       := scala213,
  crossScalaVersions := Seq(scala213, scala212),
  resolvers += "akka-snapshot-repository".at("https://repo.akka.io/snapshots")
)

lazy val `play-slick-root` = (project in file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(
    `play-slick`,
    `play-slick-evolutions`
  )
  .settings(commonSettings)
  .settings(
    Seq(
      // this overrides releaseProcess to make it work with sbt-dynver
      releaseProcess := {
        import ReleaseTransformations._
        Seq[ReleaseStep](
          checkSnapshotDependencies,
          runClean,
          releaseStepCommandAndRemaining("+test"),
          releaseStepCommandAndRemaining("+publishSigned"),
          releaseStepCommand("sonatypeBundleRelease"),
          pushChanges // <- this needs to be removed when releasing from tag
        )
      }
    )
  )

lazy val `play-slick` = (project in file("src/core"))
  .enablePlugins(PlayLibrary, Playdoc, MimaPlugin)
  .configs(Docs)
  .settings(libraryDependencies ++= Dependencies.core)
  .settings(mimaSettings)
  .settings(commonSettings)

lazy val `play-slick-evolutions` = (project in file("src/evolutions"))
  .enablePlugins(PlayLibrary, Playdoc, MimaPlugin)
  .configs(Docs)
  .settings(libraryDependencies ++= Dependencies.evolutions)
  .settings(mimaSettings)
  .settings(commonSettings)
  .dependsOn(`play-slick` % "compile;test->test")

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(PlayDocsPlugin)
  .configs(Docs)
  .dependsOn(`play-slick`)
  .dependsOn(`play-slick-evolutions`)
  .settings(commonSettings)

<<<<<<< HEAD
ThisBuild / playBuildRepoName := "play-slick"
playBuildExtraTests := {
  (samples / Test / test).value
}
=======
playBuildRepoName in ThisBuild := "play-slick"
>>>>>>> 982aa5d (Remove slick samples, migrated to play-samples repo)

// Binary compatibility is tested against this version
val previousVersion: Option[String] = Some("5.0.0")

ThisBuild / mimaFailOnNoPrevious := false

def mimaSettings = Seq(
  mimaPreviousArtifacts := previousVersion.map(organization.value %% moduleName.value % _).toSet
)
<<<<<<< HEAD

lazy val samples = project
  .in(file("samples"))
  .aggregate(
    basicSample,
    computerDatabaseSample,
    streamsSample
  )

def sampleProject(name: String) =
  Project(s"$name-sample", file("samples") / name)
    .enablePlugins(PlayScala)
    .disablePlugins(PlayFilters)
    .dependsOn(`play-slick`)
    .dependsOn(`play-slick-evolutions`)
    .settings(
      libraryDependencies += Library.playSpecs2 % "test",
      Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)
    )
    .settings(libraryDependencies += Library.h2)
    .settings(Test / javaOptions += "-Dslick.dbs.default.connectionTimeout=30 seconds")
    .settings(commonSettings)

lazy val computerDatabaseSample = sampleProject("computer-database")

lazy val streamsSample = sampleProject("streams")

lazy val basicSample = sampleProject("basic")
=======
>>>>>>> 982aa5d (Remove slick samples, migrated to play-samples repo)
