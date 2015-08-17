import sbt._
import sbt.Keys._

object Dependencies {
  val core = Seq(
    Library.slick,
    Library.hikariCP,
    Library.playJdbcApi,
    Library.playSpecs2 % "test"
  )

  val evolutions = Seq(
    Library.playJdbcEvolutions,
    Library.h2 % "test", // DBApiAdapterSpec requires a database to be available, so that a connection can be made
    Library.playSpecs2 % "test"
  )

  val resolvers = DefaultOptions.resolvers(snapshot = true) ++ Seq(
    "scalaz-releases" at "http://dl.bintray.com/scalaz/releases" // play-test -> specs2 -> scalaz-stream
  )
}

object Version {
  val play = _root_.play.core.PlayVersion.current

  val slick        = "3.1.0-M1"
  val hikariCP     = "2.3.7"
  val h2           = "1.4.187"
}

object Library {
  val playJdbcApi         = "com.typesafe.play"        %% "play-jdbc-api"            % Version.play
  val playJdbcEvolutions  = "com.typesafe.play"        %% "play-jdbc-evolutions"     % Version.play
  val playSpecs2          = "com.typesafe.play"        %% "play-specs2"              % Version.play
  val slick               = "com.typesafe.slick"       %% "slick"                    % Version.slick
  val hikariCP            = "com.zaxxer"               %  "HikariCP-java6"           % Version.hikariCP
  val h2                  = "com.h2database"           %  "h2"                       % Version.h2
}
