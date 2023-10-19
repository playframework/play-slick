import sbt._
import sbt.Keys._
import sbt.io.IO

object Playdoc extends AutoPlugin {

  object autoImport {
    final val Docs       = config("docs")
    val playdocDirectory = settingKey[File]("Base directory of play documentation")
    val playdocPackage   = taskKey[File]("Package play documentation")
  }

  import autoImport.*

  override def requires = sbt.plugins.JvmPlugin

  override def trigger = noTrigger

  override def projectSettings =
    Defaults.packageTaskSettings(playdocPackage, playdocPackage / mappings) ++
      Seq(
        playdocDirectory := (ThisBuild / baseDirectory).value / "docs" / "manual",
        playdocPackage / mappings := {
          val base: File = playdocDirectory.value
          base.allPaths.pair(IO.relativize(base.getParentFile(), _))
        },
        playdocPackage / artifactClassifier := Some("playdoc"),
        playdocPackage / artifact ~= { _.withConfigurations(Vector(Docs)) }
      ) ++
      addArtifact(playdocPackage / artifact, playdocPackage)

}
