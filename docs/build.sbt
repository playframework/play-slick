lazy val docs = project
  .in(file("."))
  .enablePlugins(PlayDocsPlugin)
  .dependsOn(playSlick)
  .settings(
    // use special snapshot play version for now
    resolvers ++= DefaultOptions.resolvers(snapshot = true),
    resolvers += Resolver.typesafeRepo("releases"),
    resolvers += "scalaz-releases" at "http://dl.bintray.com/scalaz/releases", // specs2 depends on scalaz-stream
    libraryDependencies += component("play-test") % "test",
    PlayDocsKeys.javaManualSourceDirectories := (baseDirectory.value / "manual" / "working" / "javaGuide" ** "code").get,
    PlayDocsKeys.scalaManualSourceDirectories := (baseDirectory.value / "manual" / "working" / "scalaGuide" ** "code").get
  )

lazy val playSlick = ProjectRef(file("../code"), "playSlick")
