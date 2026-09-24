resolvers ++= Seq(
  Resolver.sonatypeCentralSnapshots, // used by deploy nightlies, which publish here & use -Dplay.version
)

addSbtPlugin(
  "org.playframework" % "sbt-plugin" % sys.props.getOrElse("play.version", "3.1.0-M10-e1f3c2a9-SNAPSHOT")
)
addSbtPlugin(
  "org.playframework" % "play-docs-sbt-plugin" % sys.props.getOrElse(
    "play.version",
    "3.1.0-M10-e1f3c2a9-SNAPSHOT"
  )
)

addSbtPlugin("org.scalameta" % "sbt-scalafmt"    % "2.6.2")
addSbtPlugin("com.typesafe"  % "sbt-mima-plugin" % "1.2.1")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.12.1")
