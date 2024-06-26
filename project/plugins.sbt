resolvers ++= DefaultOptions.resolvers(snapshot = true)
resolvers ++= Resolver.sonatypeOssRepos(
  "snapshots"
) // used by deploy nightlies, which publish here & use -Dplay.version

addSbtPlugin("org.playframework" % "sbt-plugin"           % sys.props.getOrElse("play.version", "3.0.4"))
addSbtPlugin("org.playframework" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "3.0.4"))

addSbtPlugin("org.scalameta" % "sbt-scalafmt"    % "2.5.2")
addSbtPlugin("com.typesafe"  % "sbt-mima-plugin" % "1.1.3")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")
