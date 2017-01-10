import com.typesafe.tools.mima.plugin.MimaPlugin._

lazy val `play-slick-root` = (project in file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(
    `play-slick`,
    `play-slick-evolutions`
  )
  .settings(scalaVersion := "2.11.8")

lazy val `play-slick` = (project in file("src/core"))
  .enablePlugins(PlayLibrary, Playdoc)
  .settings(
    libraryDependencies ++= Dependencies.core,
    // Work around https://issues.scala-lang.org/browse/SI-9311
    scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings")),
    scalaVersion := "2.11.8"
  )
  .settings(mimaSettings)

lazy val `play-slick-evolutions` = (project in file("src/evolutions"))
  .enablePlugins(PlayLibrary, Playdoc)
  .settings(
    libraryDependencies ++= Dependencies.evolutions,
    scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings"))
  )
  .settings(mimaSettings)
  .dependsOn(`play-slick` % "compile;test->test")

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(PlayDocsPlugin)
  .dependsOn(`play-slick`)
  .dependsOn(`play-slick-evolutions`)
  .settings(scalaVersion := "2.11.8")

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
    .settings(
      libraryDependencies += Library.playSpecs2 % "test",
      concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)
    ).settings(libraryDependencies += Library.h2)
    .settings(javaOptions in Test += "-Dslick.dbs.default.connectionTimeout=30 seconds")
    .settings(scalaVersion := "2.11.8")
    .enablePlugins(PlayScala)
    .dependsOn(`play-slick`)
    .dependsOn(`play-slick-evolutions`)

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