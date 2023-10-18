// #add-library-dependencies
libraryDependencies += "org.playframework" %% "play-slick" % "6.0.0"
// #add-library-dependencies

// #add-dependency-with-evolutions
libraryDependencies ++= Seq(
  "org.playframework" %% "play-slick"            % "6.0.0",
  "org.playframework" %% "play-slick-evolutions" % "6.0.0"
)
// #add-dependency-with-evolutions
