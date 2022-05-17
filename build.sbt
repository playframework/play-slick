import scala.sys.process._
import com.typesafe.tools.mima.plugin.MimaPlugin._
import interplay.ScalaVersions._

resolvers in ThisBuild += Resolver.sonatypeRepo("releases")

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
dynverVTagPrefix in ThisBuild := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

lazy val commonSettings = Seq(
  // Work around https://issues.scala-lang.org/browse/SI-9311
  scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings")),
  scalaVersion := scala213,
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

// Binary compatibility is tested against this version
val previousVersion: Option[String] = Some("5.0.0")

ThisBuild / mimaFailOnNoPrevious := false

def mimaSettings = mimaDefaultSettings ++ Seq(
  mimaPreviousArtifacts := previousVersion.map(organization.value %% moduleName.value % _).toSet
)
