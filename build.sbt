lazy val `play-slick-root` = (project in file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(
    `play-slick`,
    `play-slick-evolutions`
  )

lazy val `play-slick` = (project in file("src/core"))
  .enablePlugins(PlayLibrary, Playdoc)
  .settings(
    libraryDependencies ++= Dependencies.core,
    // Work around https://issues.scala-lang.org/browse/SI-9311
    scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings"))
  )

lazy val `play-slick-evolutions` = (project in file("src/evolutions"))
  .enablePlugins(PlayLibrary, Playdoc)
  .settings(
    libraryDependencies ++= Dependencies.evolutions,
    scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings"))
  ).dependsOn(`play-slick` % "compile;test->test")

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(PlayDocsPlugin)
  .dependsOn(`play-slick`)
  .dependsOn(`play-slick-evolutions`)

playBuildRepoName in ThisBuild := "play-slick"
playBuildExtraTests := {
  (test in (samples, Test)).value
}

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

def sampleProject(name: String) =
  Project(s"$name-sample", file("samples") / name)
    .settings(
      libraryDependencies += Library.playSpecs2 % "test",
      concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)
    ).settings(libraryDependencies += "com.h2database" % "h2" % "1.4.187")
    .enablePlugins(PlayScala)
    .dependsOn(`play-slick`)
    .dependsOn(`play-slick-evolutions`)

lazy val daoSample = sampleProject("dao")

lazy val computerDatabaseSample = sampleProject("computer-database")

lazy val iterateeSample = sampleProject("iteratee")

lazy val jsonSample = sampleProject("json")

lazy val basicSample = sampleProject("basic")

lazy val diSample = sampleProject("di")
