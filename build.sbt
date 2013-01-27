name := "play-slick"

version := "0.2.7-SNAPSHOT"

organization := "com.typesafe"

scalaVersion := "2.10.0"

libraryDependencies ++= Seq(
  "play" %% "play" % "2.1-RC2",
  "play" %% "play-jdbc" % "2.1-RC2",
  "com.typesafe.slick" % "slick_2.10" % "1.0.0-RC2")

