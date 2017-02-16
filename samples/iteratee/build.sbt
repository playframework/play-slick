name := "iteratee-sample"

PlayKeys.playOmnidoc := false

libraryDependencies += "org.joda" % "joda-convert" % "1.8.1"
libraryDependencies += "com.typesafe.play" %% "play-streams" % Version.play
libraryDependencies += "com.typesafe.play" %% "play-iteratees" % "2.6.1"
libraryDependencies += "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1"
libraryDependencies += guice
