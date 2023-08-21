resolvers ++= DefaultOptions.resolvers(snapshot = true)
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"), // used by deploy nightlies, which publish here & use -Dplay.version
)

addSbtPlugin("com.typesafe.play" % "sbt-plugin"           % sys.props.getOrElse("play.version", "2.9.0-M7"))
addSbtPlugin("com.typesafe.play" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "2.9.0-M7"))
addSbtPlugin("com.typesafe.play" % "interplay"            % sys.props.get("interplay.version").getOrElse("3.1.2"))

addSbtPlugin("org.scalameta" % "sbt-scalafmt"    % "2.5.0")
addSbtPlugin("com.typesafe"  % "sbt-mima-plugin" % "1.1.3")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")
