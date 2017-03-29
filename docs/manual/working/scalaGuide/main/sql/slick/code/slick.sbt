// #add-library-dependencies
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0-M3"
// #add-library-dependencies

// #add-dependency-with-evolutions
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.0-M3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0-M3"
)
// #add-dependency-with-evolutions
