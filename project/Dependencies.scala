import sbt._

object Dependencies {
  val core = Seq(
    Library.slick,
    Library.slickHikariCP,
    Library.playJdbcApi,
    Library.playLogback % "test",
    Library.playSpecs2  % "test",
    Library.h2          % "test"
  )

  val evolutions = Seq(
    Library.playJdbcEvolutions,
    Library.h2         % "test", // DBApiAdapterSpec requires a database to be available, so that a connection can be made
    Library.playSpecs2 % "test"
  )
}

object Version {
  val play = _root_.play.core.PlayVersion.current

  val slick = "3.3.2"
  val h2    = "1.4.200"
}

object Library {
  val playLogback        = "com.typesafe.play"  %% "play-logback"         % Version.play
  val playJdbcApi        = "com.typesafe.play"  %% "play-jdbc-api"        % Version.play
  val playJdbcEvolutions = "com.typesafe.play"  %% "play-jdbc-evolutions" % Version.play
  val playSpecs2         = "com.typesafe.play"  %% "play-specs2"          % Version.play
  val slick              = "com.typesafe.slick" %% "slick"                % Version.slick
  val slickHikariCP      = "com.typesafe.slick" %% "slick-hikaricp"       % Version.slick
  val h2                 = "com.h2database"     % "h2"                    % Version.h2
}
