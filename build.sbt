import scala.sys.process._
import com.typesafe.tools.mima.plugin.MimaPlugin._
import interplay.ScalaVersions._

resolvers in ThisBuild += Resolver.sonatypeRepo("releases")

lazy val commonSettings = Seq(
  // Work around https://issues.scala-lang.org/browse/SI-9311
  scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings")),
  scalaVersion := scala213,
  crossScalaVersions := Seq(scala213, scala212),
  resolvers += Resolver.bintrayRepo("akka", "snapshots")
)

lazy val `play-slick-root` = (project in file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(
    `play-slick`,
    `play-slick-evolutions`
  )
  .settings(commonSettings)

lazy val `play-slick` = (project in file("src/core"))
  .enablePlugins(PlayLibrary, Playdoc)
  .configs(Docs)
  .settings(libraryDependencies ++= Dependencies.core)
  .settings(mimaSettings)
  .settings(commonSettings)

lazy val `play-slick-evolutions` = (project in file("src/evolutions"))
  .enablePlugins(PlayLibrary, Playdoc)
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

playBuildRepoName in ThisBuild := "play-slick"
playBuildExtraTests := {
  (test in (samples, Test)).value
}

// Binary compatibility is tested against this version
val previousVersion: Option[String] = Some("5.0.0")

ThisBuild / mimaFailOnNoPrevious := false

def mimaSettings = mimaDefaultSettings ++ Seq(
  mimaPreviousArtifacts := previousVersion.map(organization.value %% moduleName.value % _).toSet
)

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
      concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)
    )
    .settings(libraryDependencies += Library.h2)
    .settings(javaOptions in Test += "-Dslick.dbs.default.connectionTimeout=30 seconds")
    .settings(commonSettings)

lazy val computerDatabaseSample = sampleProject("computer-database")

lazy val streamsSample = sampleProject("streams")

lazy val basicSample = sampleProject("basic")
