// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
//This is normally correct, but M2 is a SNAPSHOT and therefore we have to override
//addSbtPlugin("play" % "sbt-plugin" % Option(System.getProperty("play.version")).getOrElse("2.0"))
addSbtPlugin("play" % "sbt-plugin" % "2.2-SNAPSHOT")
