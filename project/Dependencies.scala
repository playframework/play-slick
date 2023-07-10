import sbt._
import Keys._

object Dependencies {
  val core = Def.setting(
    Seq(
      Library.slick.value,
      Library.slickHikariCP.value,
      Library.playCore,
      Library.playJdbcApi,
      Library.playLogback % "test",
      Library.playSpecs2  % "test",
      Library.h2          % "test"
    )
  )

  val evolutions = Seq(
    Library.playJdbcEvolutions,
    Library.h2 % "test", // DBApiAdapterSpec requires a database to be available, so that a connection can be made
    Library.playSpecs2 % "test"
  )
}

object Version {
  val play = _root_.play.core.PlayVersion.current

  val slick = Def.setting(
    if (scalaBinaryVersion.value == "3") {
      "3.5.0-M4"
    } else {
      "3.4.1"
    }
  )
  val h2 = "2.2.220"
}

object Library {
  val playLogback        = "com.typesafe.play" %% "play-logback"         % Version.play
  val playCore           = "com.typesafe.play" %% "play"                 % Version.play
  val playJdbcApi        = "com.typesafe.play" %% "play-jdbc-api"        % Version.play
  val playJdbcEvolutions = "com.typesafe.play" %% "play-jdbc-evolutions" % Version.play
  val playSpecs2         = "com.typesafe.play" %% "play-specs2"          % Version.play
  val slick              = Def.setting("com.typesafe.slick" %% "slick" % Version.slick.value)
  val slickHikariCP      = Def.setting("com.typesafe.slick" %% "slick-hikaricp" % Version.slick.value)
  val h2                 = "com.h2database"     % "h2"                   % Version.h2
}
