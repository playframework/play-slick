resolvers ++= DefaultOptions.resolvers(snapshot = true)
resolvers ++= Resolver.sonatypeOssRepos(
  "snapshots"
) // used by deploy nightlies, which publish here & use -Dplay.version

addSbtPlugin("org.playframework" % "sbt-plugin"           % sys.props.getOrElse("play.version", "3.1.0-M1"))
addSbtPlugin("org.playframework" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "3.1.0-M1"))

addSbtPlugin("org.scalameta" % "sbt-scalafmt"    % "2.5.4")
addSbtPlugin("com.typesafe"  % "sbt-mima-plugin" % "1.1.4")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.9.2")
