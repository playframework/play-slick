import scala.sys.process._
import com.typesafe.tools.mima.plugin.MimaPlugin._
import interplay.ScalaVersions._

lazy val commonSettings = Seq(
  // Work around https://issues.scala-lang.org/browse/SI-9311
  scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings")),
  scalaVersion := scala212,
  crossScalaVersions := Seq(scala211, scala212)
)

lazy val `play-slick-root` = (project in file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(
    `play-slick`,
    `play-slick-evolutions`
  )
  .settings(commonSettings: _*)

lazy val `play-slick` = (project in file("src/core"))
  .enablePlugins(PlayLibrary, Playdoc)
  .settings(libraryDependencies ++= Dependencies.core)
  .settings(mimaSettings)
  .settings(commonSettings: _*)

lazy val `play-slick-evolutions` = (project in file("src/evolutions"))
  .enablePlugins(PlayLibrary, Playdoc)
  .settings(libraryDependencies ++= Dependencies.evolutions)
  .settings(mimaSettings)
  .settings(commonSettings: _*)
  .dependsOn(`play-slick` % "compile;test->test")

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(PlayDocsPlugin)
  .dependsOn(`play-slick`)
  .dependsOn(`play-slick-evolutions`)
  .settings(commonSettings: _*)

playBuildRepoName in ThisBuild := "play-slick"
playBuildExtraTests := {
  (test in (samples, Test)).value
}

// Binary compatibility is tested against this version
val previousVersion: Option[String] = None

def mimaSettings = mimaDefaultSettings ++ Seq(
  mimaPreviousArtifacts := Set(previousVersion flatMap { previousVersion =>
    if (crossPaths.value) Some(organization.value % s"${moduleName.value}_${scalaBinaryVersion.value}" % previousVersion)
    else Some(organization.value % moduleName.value % previousVersion)
  }).flatten
)

lazy val samples = project
  .in(file("samples"))
  .aggregate(
    basicSample,
    computerDatabaseSample,
    iterateeSample
  )

def sampleProject(name: String) =
  Project(s"$name-sample", file("samples") / name)
    .enablePlugins(PlayScala)
    .disablePlugins(PlayFilters)
    .dependsOn(`play-slick`)
    .dependsOn(`play-slick-evolutions`)
    .settings(
      libraryDependencies ++= Seq(
        Library.playSpecs2 % "test",
        // This could be removed after releasing https://github.com/playframework/playframework/pull/7266
        "org.fluentlenium" % "fluentlenium-core" % "3.2.0"
      ),
      concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)
    ).settings(libraryDependencies += Library.h2)
    .settings(javaOptions in Test += "-Dslick.dbs.default.connectionTimeout=30 seconds")
    .settings(commonSettings: _*)

lazy val computerDatabaseSample = sampleProject("computer-database")

lazy val iterateeSample = sampleProject("iteratee")

lazy val basicSample = sampleProject("basic")

lazy val checkCodeFormat = taskKey[Unit]("Check that code format is following Scalariform rules")

checkCodeFormat := {
  val exitCode = "git diff --exit-code".!
  if (exitCode != 0) {
    sys.error(
      """
        |ERROR: Scalariform check failed, see differences above.
        |To fix, format your sources using sbt scalariformFormat test:scalariformFormat before submitting a pull request.
        |Additionally, please squash your commits (eg, use git commit --amend) if you're going to update this pull request.
        |""".stripMargin)
  }
}

addCommandAlias("validateCode", ";scalariformFormat;checkCodeFormat")
