import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "computer-database-slick"
    val appVersion      = "1.0"

    val appDependencies = Seq(
    	jdbc
    )

    
  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalaVersion := "2.10.2"
    // Add your own project settings here      
  ).dependsOn(RootProject(file("../../")))


}
            
