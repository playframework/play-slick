import sbt._
import sbt.Keys._

object Omnidoc {
  val packagePlaydoc = taskKey[File]("Package play documentation")

  def settings = sourceUrlSettings ++ playdocSettings

  val sourceUrlSettings = Seq(
    projectID := {
      val baseUrl = "https://github.com/playframework/play-slick"
      val sourceTree = if (isSnapshot.value) "master" else ("v" + version.value)
      val sourceUrl = s"${baseUrl}/tree/${sourceTree}/code"
      projectID.value.extra("info.sourceUrl" -> sourceUrl)
    }
  )

  val playdocSettings =
    Defaults.packageTaskSettings(packagePlaydoc, mappings in packagePlaydoc) ++
    Seq(
      mappings in packagePlaydoc := {
        val base = baseDirectory.value / "docs"
        (base / "manual").***.get pair relativeTo(base)
      },
      artifactClassifier in packagePlaydoc := Some("playdoc"),
      artifact in packagePlaydoc ~= { _.copy(configurations = Seq(Docs)) }
    ) ++
    addArtifact(artifact in packagePlaydoc, packagePlaydoc)
}
