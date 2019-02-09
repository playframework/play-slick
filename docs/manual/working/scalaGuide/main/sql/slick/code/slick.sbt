// #add-library-dependencies
libraryDependencies += "com.typesafe.play" %% "play-slick" % "4.0.0"
// #add-library-dependencies

// #add-dependency-with-evolutions
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0"
)
// #add-dependency-with-evolutions
