import sbt.*

object Dependencies {
  def core(scalaVersion: Option[(Long, Long)]) = (scalaVersion match {
    case Some((3, _)) => Seq(Library.slick(Version.slick_35), Library.slickHikariCP(Version.slick_35))
    case _            => Seq(Library.slick(Version.slick), Library.slickHikariCP(Version.slick))
  }) ++ Seq(
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

  val slick    = "3.4.1"
  val slick_35 = "3.5.0-M4"
  val h2       = "2.2.224"
}

object Library {
  val playLogback                    = "org.playframework"  %% "play-logback"         % Version.play
  val playCore                       = "org.playframework"  %% "play"                 % Version.play
  val playJdbcApi                    = "org.playframework"  %% "play-jdbc-api"        % Version.play
  val playJdbcEvolutions             = "org.playframework"  %% "play-jdbc-evolutions" % Version.play
  val playSpecs2                     = "org.playframework"  %% "play-specs2"          % Version.play
  def slick(version: String)         = "com.typesafe.slick" %% "slick"                % version
  def slickHikariCP(version: String) = "com.typesafe.slick" %% "slick-hikaricp"       % version
  val h2                             = "com.h2database"      % "h2"                   % Version.h2
}
