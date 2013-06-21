import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play-slick-sample"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalaVersion := "2.10.2"
    // Add your own project settings here      
  ).dependsOn(RootProject(file("../../")))

}
