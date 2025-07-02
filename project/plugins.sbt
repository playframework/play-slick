resolvers ++= DefaultOptions.resolvers(snapshot = true)
resolvers ++= Resolver.sonatypeOssRepos(
  "snapshots"
) // used by deploy nightlies, which publish here & use -Dplay.version

addSbtPlugin("org.playframework" % "sbt-plugin"           % sys.props.getOrElse("play.version", "3.0.8"))
addSbtPlugin("org.playframework" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "3.0.8"))

addSbtPlugin("org.scalameta" % "sbt-scalafmt"    % "2.5.5")
addSbtPlugin("com.typesafe"  % "sbt-mima-plugin" % "1.1.4")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.1")
