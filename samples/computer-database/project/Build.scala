import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName         = "computer-database-slick"
  val appVersion      = "1.0"

  val appDependencies = Seq(
    jdbc
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    scalaVersion := "2.10.2",
    version := appVersion,
    libraryDependencies ++= appDependencies
  ).dependsOn(ProjectRef(file("../../code"), "playSlick"))

}
            
