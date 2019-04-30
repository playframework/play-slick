resolvers ++= DefaultOptions.resolvers(snapshot = true)

resolvers += Resolver.sonatypeRepo("snapshots")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % sys.props.getOrElse("play.version", "2.7.2"))
addSbtPlugin("com.typesafe.play" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "2.7.2"))
addSbtPlugin("com.typesafe.play" % "interplay" % sys.props.get("interplay.version").getOrElse("2.0.6"))
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.3.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
