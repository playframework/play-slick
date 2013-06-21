name := "play-slick"

licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

homepage := Some(url("https://github.com/freekh/play-slick"))

version := "0.3.3-SNAPSHOT"

organization := "com.typesafe.play"

scalaVersion := "2.10.2"

resolvers += Classpaths.sbtPluginReleases

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

scalacOptions += "-feature"

libraryDependencies ++= {
  val playVersion = "2.1.1"
  Seq(
  "play" %% "play" % playVersion,
  "play" %% "play-jdbc" % playVersion,
  "com.typesafe.slick" %% "slick" % "1.0.1",
  "play" %% "play-test" % playVersion % "test")
}

