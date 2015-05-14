import sbt._
import sbt.Keys._

object CommonSettings extends AutoPlugin {

  override def requires = plugins.JvmPlugin
  override def trigger  = allRequirements

  override def projectSettings = Seq(
    parallelExecution in Test := false,
    resolvers ++= Dependencies.resolvers,
    fork in Test := true
  )
}
