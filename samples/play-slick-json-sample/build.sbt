name := "play-slick-json-sample"

version := "1.0-SNAPSHOT"

lazy val root = Project("play-slick-json-sample", file("."))
  .enablePlugins(PlayScala)
  .dependsOn(ProjectRef(file("../../code"), "playSlick"))

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc
)
