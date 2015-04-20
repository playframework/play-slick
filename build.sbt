name := "play-slick"

lazy val commonSettings = Seq(
  libraryDependencies += Library.playSpecs2 % "test",
  concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)
)

lazy val playSlick = project
  .in(file("."))
  .settings(commonSettings: _*)
  .enablePlugins(Playdoc, Omnidoc)

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(PlayDocsPlugin)
  .dependsOn(playSlick)

lazy val samples = project
  .in(file("samples"))
  .aggregate(
    daoSample,
    computerDatabaseSample,
    iterateeSample,
    jsonSample,
    basicSample,
    diSample
  )

def sampleProject(name: String) = (
  Project(s"$name-sample", file("samples") / name)
  settings(commonSettings: _*)
  enablePlugins(PlayScala)
  dependsOn(playSlick)
)

lazy val daoSample = sampleProject("dao")

lazy val computerDatabaseSample = sampleProject("computer-database")

lazy val iterateeSample = sampleProject("iteratee")

lazy val jsonSample = sampleProject("json")

lazy val basicSample = sampleProject("basic")

lazy val diSample = sampleProject("di")

Publish.settings
Release.settings

libraryDependencies ++= Dependencies.playSlick

OmnidocKeys.githubRepo := "playframework/play-slick"