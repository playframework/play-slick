import scala.sys.process._

import com.typesafe.tools.mima.plugin.MimaPlugin._
import com.typesafe.tools.mima.core._
import interplay.ScalaVersions._

ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("releases")

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
ThisBuild / dynverVTagPrefix := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

lazy val commonSettings = Seq(
  // Work around https://issues.scala-lang.org/browse/SI-9311
  scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings")),
  scalaVersion       := "2.13.13",
  crossScalaVersions := Seq("2.13.13", "3.3.3"),
  pomExtra           := scala.xml.NodeSeq.Empty, // Can be removed when dropping interplay
  developers += Developer(
    "playframework",
    "The Play Framework Contributors",
    "contact@playframework.com",
    url("https://github.com/playframework")
  ),
)

lazy val `play-slick-root` = (project in file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(
    `play-slick`,
    `play-slick-evolutions`
  )
  .settings(commonSettings)

lazy val `play-slick` = (project in file("src/core"))
  .enablePlugins(PlayLibrary, Playdoc, MimaPlugin)
  .configs(Docs)
  .settings(libraryDependencies ++= Dependencies.core)
  .settings(mimaSettings)
  .settings(
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[DirectMissingMethodProblem]("play.api.db.slick.HasDatabaseConfig.db"),
    )
  )
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

ThisBuild / playBuildRepoName := "play-slick"

// Binary compatibility is tested against this version
val previousVersion: Option[String] = Some("5.2.0")

ThisBuild / mimaFailOnNoPrevious := false

def mimaSettings = Seq(
  mimaPreviousArtifacts := (if (CrossVersion.binaryScalaVersion(scalaVersion.value) == "3") {
                              Set.empty
                            } else {
                              previousVersion.map(organization.value %% moduleName.value % _).toSet
                            }),
  mimaBinaryIssueFilters := Seq(
  )
)
