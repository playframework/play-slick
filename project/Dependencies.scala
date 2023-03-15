import sbt._

object Dependencies {
  val core = Seq(
    Library.playCore,
    Library.playJdbcApi,
    Library.playLogback % "test",
    Library.playSpecs2  % "test",
    Library.h2          % "test"
  )

  val scala2Deps = Seq(
    Library.slick2,
    Library.slickHikariCP2,
  )

  val scala3Deps = Seq(
    Library.slick3,
    Library.slickHikariCP3,
  )

  val evolutions = Seq(
    Library.playJdbcEvolutions,
    Library.h2 % "test", // DBApiAdapterSpec requires a database to be available, so that a connection can be made
    Library.playSpecs2 % "test"
  )
}

object Version {
  val play = _root_.play.core.PlayVersion.current

  val slick2 = "3.4.1"
  val slick3 = "3.4.0-SNAPSHOT"
  val h2     = "2.1.214"
}

object Library {
  val playLogback        = "com.typesafe.play"  %% "play-logback"         % Version.play
  val playCore           = "com.typesafe.play"  %% "play"                 % Version.play
  val playJdbcApi        = "com.typesafe.play"  %% "play-jdbc-api"        % Version.play
  val playJdbcEvolutions = "com.typesafe.play"  %% "play-jdbc-evolutions" % Version.play
  val playSpecs2         = "com.typesafe.play"  %% "play-specs2"          % Version.play
  val slick2             = "com.typesafe.slick" %% "slick"                % Version.slick2
  val slick3             = "com.typesafe.slick" %% "slick"                % Version.slick3
  val slickHikariCP2     = "com.typesafe.slick" %% "slick-hikaricp"       % Version.slick2
  val slickHikariCP3     = "com.typesafe.slick" %% "slick-hikaricp"       % Version.slick3
  val h2                 = "com.h2database"      % "h2"                   % Version.h2
}
