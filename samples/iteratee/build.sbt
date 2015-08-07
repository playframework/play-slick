name := "iteratee-sample"

PlayKeys.playOmnidoc := false

libraryDependencies += "com.typesafe.play" %% "play-streams-experimental" % Version.play

javaOptions in Test += "-Dconfig.file=conf/application-test.conf"