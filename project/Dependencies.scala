import sbt._
import sbt.Keys._

object Dependencies {
  val playSlick = Seq(
    Library.playJdbc,
    Library.slick,
    Library.playSpecs2 % "test"
  )

  val resolvers = DefaultOptions.resolvers(snapshot = true) ++ Seq(
    Resolver.typesafeRepo("releases"),
    "scalaz-releases" at "http://dl.bintray.com/scalaz/releases" // play-test -> specs2 -> scalaz-stream
  )
}

object Version {
  val play = _root_.play.core.PlayVersion.current

  val slick        = "3.0.0"
}

object Library {
  val playJdbc     = "com.typesafe.play"        %% "play-jdbc"         % Version.play
  val playSpecs2   = "com.typesafe.play"        %% "play-specs2"       % Version.play
  val slick        = "com.typesafe.slick"       %% "slick"             % Version.slick
}
