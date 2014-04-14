
lazy val playSlick = ProjectRef(file("code/"), "playSlick")

lazy val computerDatabase = ProjectRef(file("samples/computer-database"), "computer-database-slick")

lazy val playSlickCakeSample = ProjectRef(file("samples/play-slick-cake-sample"), "play-slick-cake-sample")

lazy val playSlickIterateeSample = ProjectRef(file("samples/play-slick-iteratee-sample"), "play-slick-iteratee-sample")

lazy val playSlickSample = ProjectRef(file("samples/play-slick-sample"), "play-slick-sample")

name := "play-slick-project"

parallelExecution in Test := false

lazy val root = project.in(file(".")).aggregate(playSlick, playSlickSample, computerDatabase, playSlickCakeSample, playSlickIterateeSample)

