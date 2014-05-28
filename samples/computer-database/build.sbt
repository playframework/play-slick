name := "computer-database-slick"

version := "1.0-SNAPSHOT"

lazy val root = Project("computer-database-slick", file("."))
  .enablePlugins(PlayScala)
  .dependsOn(ProjectRef(file("../../code"), "playSlick"))

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc
)
