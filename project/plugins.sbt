// use special snapshot play version for now

resolvers ++= DefaultOptions.resolvers(snapshot = true)

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("com.typesafe.play" % "play-docs-sbt-plugin" % "2.4-2014-11-04-10ce984-SNAPSHOT")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
