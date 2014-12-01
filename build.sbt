name := "play-slick"

lazy val playSlick = project
  .in(file("."))

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(PlayDocsPlugin)
  .dependsOn(playSlick)

lazy val samples = project
  .in(file("samples"))
  .aggregate(
    sampleCake,
    sampleComputerDatabase,
    sampleIteratee,
    sampleJson,
    samplePlaySlick
  )

lazy val sampleCake = project
  .in(file("samples") / "cake")
  .enablePlugins(PlayScala)
  .dependsOn(playSlick)

lazy val sampleComputerDatabase = project
  .in(file("samples") / "computer-database")
  .enablePlugins(PlayScala)
  .dependsOn(playSlick)

lazy val sampleIteratee = project
  .in(file("samples") / "iteratee")
  .enablePlugins(PlayScala)
  .dependsOn(playSlick)

lazy val sampleJson = project
  .in(file("samples") / "json")
  .enablePlugins(PlayScala)
  .dependsOn(playSlick)

lazy val samplePlaySlick = project
  .in(file("samples") / "sample")
  .enablePlugins(PlayScala)
  .dependsOn(playSlick)

Publish.settings
Omnidoc.settings
Release.settings

libraryDependencies ++= Dependencies.playSlick
