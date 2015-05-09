name := "play-slick"

lazy val root = Project("root",file("."))
  .aggregate(
    core,
    evolutions
  )

lazy val commonSettings = Seq(
  libraryDependencies += Library.playSpecs2 % "test",
  concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)
)

lazy val core = Project("play-slick", file("src/core"))
  .settings(libraryDependencies ++= Dependencies.core)
  .enablePlugins(Playdoc, Omnidoc)

lazy val evolutions = Project("play-slick-evolutions", file("src/evolutions"))
  .settings(libraryDependencies ++= Dependencies.evolutions)
  .dependsOn(core % "compile;test->test")
  .enablePlugins(Playdoc, Omnidoc)

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(PlayDocsPlugin)
  .dependsOn(core)
  .dependsOn(evolutions)

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
  settings(libraryDependencies += "com.h2database" % "h2" % "1.4.187")
  enablePlugins(PlayScala)
  dependsOn(core)
  dependsOn(evolutions) // this is because all samples currently use evolutions
)

lazy val daoSample = sampleProject("dao")

lazy val computerDatabaseSample = sampleProject("computer-database")

lazy val iterateeSample = sampleProject("iteratee")

lazy val jsonSample = sampleProject("json")

lazy val basicSample = sampleProject("basic")

lazy val diSample = sampleProject("di")

Publish.settings
Release.settings

OmnidocKeys.githubRepo := "playframework/play-slick"