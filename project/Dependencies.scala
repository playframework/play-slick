import sbt._

object Dependencies {
  val scala213Version   = "2.13.18"
  val scala3Version     = "3.3.8"
  val scala39LTSVersion = "3.9.0"
  val scala3NextVersion = "3.10.0-RC2"

  val publishedScalaVersions = Seq(scala213Version, scala3Version)

  private val scalaVersionAliases = Map(
    "2.13.x" -> scala213Version,
    "3.3.x"  -> scala3Version,
    "3.9.x"  -> scala39LTSVersion,
    "3.next" -> scala3NextVersion,
  )

  def resolveScalaVersion(version: String): String = scalaVersionAliases.getOrElse(version, version)

  val core = Seq(
    Library.slick,
    Library.slickHikariCP,
    Library.playCore,
    Library.playJdbcApi,
    Library.playLogback % "test",
    Library.playSpecs2  % "test",
    Library.h2          % "test"
  )

  val evolutions = Seq(
    Library.playJdbcEvolutions,
    Library.h2 % "test", // DBApiAdapterSpec requires a database to be available, so that a connection can be made
    Library.playSpecs2 % "test"
  )
}

object Version {
  val play = _root_.play.core.PlayVersion.current

  val slick = "3.6.1"
  val h2    = "2.5.250"
}

object Library {
  val playLogback        = "org.playframework"  %% "play-logback"         % Version.play
  val playCore           = "org.playframework"  %% "play"                 % Version.play
  val playJdbcApi        = "org.playframework"  %% "play-jdbc-api"        % Version.play
  val playJdbcEvolutions = "org.playframework"  %% "play-jdbc-evolutions" % Version.play
  val playSpecs2         = "org.playframework"  %% "play-specs2"          % Version.play
  val slick              = "com.typesafe.slick" %% "slick"                % Version.slick
  val slickHikariCP      = "com.typesafe.slick" %% "slick-hikaricp"       % Version.slick
  val h2                 = "com.h2database"      % "h2"                   % Version.h2
}
