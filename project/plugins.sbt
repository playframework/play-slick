resolvers ++= DefaultOptions.resolvers(snapshot = true)

addSbtPlugin("com.typesafe.play" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "2.4.0-2015-05-10-ed330de-SNAPSHOT"))
addSbtPlugin("com.typesafe.play" % "interplay" % "1.0.1")
