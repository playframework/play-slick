resolvers ++= DefaultOptions.resolvers(snapshot = true)

addSbtPlugin("com.typesafe.play" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "2.4.0"))
addSbtPlugin("com.typesafe.play" % "interplay" % sys.props.get("interplay.version").getOrElse("1.0.2"))
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.7")
