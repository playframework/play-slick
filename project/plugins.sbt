resolvers ++= DefaultOptions.resolvers(snapshot = true)

resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("com.typesafe.play" % "sbt-plugin"           % sys.props.getOrElse("play.version", "2.8.0-RC5"))
addSbtPlugin("com.typesafe.play" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "2.8.0-RC5"))
addSbtPlugin("com.typesafe.play" % "interplay"            % sys.props.get("interplay.version").getOrElse("2.1.4"))

addSbtPlugin("org.scalameta" % "sbt-scalafmt"    % "2.2.1")
addSbtPlugin("com.typesafe"  % "sbt-mima-plugin" % "0.6.1")
