name := "play-slick"

licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

homepage := Some(url("https://github.com/freekh/play-slick"))

version := "0.5.0.2-SNAPSHOT"

organization := "com.typesafe.play"

scalaVersion := "2.10.2"

resolvers += Classpaths.sbtPluginReleases

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

scalacOptions += "-feature"

libraryDependencies ++= {
  val playVersion = "2.2.0-M2"
  Seq(
    "com.typesafe.play" %% "play" % playVersion,
    "com.typesafe.play" %% "play-java" % playVersion,
    "com.typesafe.play" %% "play-jdbc" % playVersion,
    "com.typesafe.slick" %% "slick" % "1.0.1",
    "com.typesafe.play" %% "play-test" % playVersion % "test")
}

