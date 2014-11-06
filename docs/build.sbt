lazy val docs = project
  .in(file("."))
  .enablePlugins(PlayDocsPlugin)
  .dependsOn(playSlick)
  .settings(
    libraryDependencies += component("play-test") % "test",
    PlayDocsKeys.javaManualSourceDirectories := (baseDirectory.value / "manual" / "working" / "javaGuide" ** "code").get,
    PlayDocsKeys.scalaManualSourceDirectories := (baseDirectory.value / "manual" / "working" / "scalaGuide" ** "code").get
  )

lazy val playSlick = ProjectRef(file("../code"), "playSlick")
