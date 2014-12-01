import sbt._
import sbt.Keys._

object CommonSettings extends AutoPlugin {

  override def requires = plugins.JvmPlugin
  override def trigger  = allRequirements

  override def projectSettings = Seq(
    organization := "com.typesafe.play",
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.10.4", scalaVersion.value),
    scalacOptions ++= Seq("-feature", "-deprecation"),
    parallelExecution in Test := false,
    resolvers ++= Dependencies.resolvers
  )
}
