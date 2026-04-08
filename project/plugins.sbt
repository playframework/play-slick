resolvers ++= DefaultOptions.resolvers(snapshot = true)
resolvers ++= Seq(
  Resolver.sonatypeCentralSnapshots, // used by deploy nightlies, which publish here & use -Dplay.version
)

addSbtPlugin("org.playframework" % "sbt-plugin"           % sys.props.getOrElse("play.version", "3.1.0-M6"))
addSbtPlugin("org.playframework" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "3.1.0-M6"))

addSbtPlugin("org.scalameta" % "sbt-scalafmt"    % "2.5.6")
addSbtPlugin("com.typesafe"  % "sbt-mima-plugin" % "1.1.5")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.2")
