name := "play-slick"

version := "0.2.7-SNAPSHOT"

organization := "com.typesafe"

scalaVersion := "2.10.0"

resolvers += Classpaths.sbtPluginReleases

libraryDependencies ++= {
  val playVersion = "2.1.0"
  Seq(
  "play" %% "play" % playVersion,
  "play" %% "play-jdbc" % playVersion,
  "com.typesafe.slick" % "slick_2.10" % "1.0.0",
  "play" %% "play-test" % playVersion % "test")
}

