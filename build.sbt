name := "play-slick"

version := "0.2.7-SNAPSHOT"

organization := "com.typesafe"

scalaVersion := "2.10.0" //RC1 to harmonize with Play RC1

libraryDependencies ++= Seq(
  "play" %% "play" % "2.1.0",
  "play" %% "play-jdbc" % "2.1.0",
  "com.typesafe.slick" % "slick_2.10" % "1.0.0")

