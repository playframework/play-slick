import scala.sys.process._
import com.typesafe.tools.mima.plugin.MimaPlugin._
import com.typesafe.tools.mima.core._

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
  organization         := "com.typesafe.play",
  organizationName     := "The Play Framework Project",
  organizationHomepage := Some(url("https://playframework.com/")),
  homepage             := Some(url(s"https://github.com/playframework/${Common.repoName}")),
  licenses             := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  javacOptions ++= Seq("-encoding", "UTF-8", "-Xlint:-options"),
  compile / javacOptions ++= Seq("--release", "11"),
  doc / javacOptions := Seq("-source", "11"),
  scalaVersion       := "2.13.12",               // scala213,
  crossScalaVersions := Seq("2.13.12", "3.3.1"), // scala213,
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8") ++
    (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 13)) => Seq("-Xsource:3", "-Xmigration")
      case _             => Seq.empty
    }),
  developers += Developer(
    "playframework",
    "The Play Framework Contributors",
    "contact@playframework.com",
    url("https://github.com/playframework")
  ),
  pomIncludeRepository := { _ => false }
)

lazy val `play-slick-root` = (project in file("."))
  .aggregate(
    `play-slick`,
    `play-slick-evolutions`
  )
  .settings(commonSettings)
  .settings(
    publish / skip := true
  )

lazy val `play-slick` = (project in file("src/core"))
  .enablePlugins(Omnidoc, Playdoc, MimaPlugin)
  .configs(Docs)
  .settings(libraryDependencies ++= Dependencies.core)
  .settings(mimaSettings)
  .settings(
    mimaBinaryIssueFilters ++= Seq(
    )
  )
  .settings(commonSettings)

lazy val `play-slick-evolutions` = (project in file("src/evolutions"))
  .enablePlugins(Omnidoc, Playdoc, MimaPlugin)
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

// Binary compatibility is tested against this version
val previousVersion: Option[String] = Some("5.2.0-RC1")

ThisBuild / mimaFailOnNoPrevious := false

def mimaSettings = Seq(
  mimaPreviousArtifacts := previousVersion.map(organization.value %% moduleName.value % _).toSet
)
