import sbt._
import sbt.Keys._

object Dependencies {
  val playSlick = Seq(
    Library.playJdbc,
    Library.slick,
    Library.javaxServlet,
    Library.findbugs,
    Library.reflections.notTransitive,
    Library.playTest % "test",
    Library.slickTestkit % "test",
    Library.hsqldb % "test",
    Library.mockito % "test"
  )

  val resolvers = DefaultOptions.resolvers(snapshot = true) ++ Seq(
    Resolver.typesafeRepo("releases"),
    "scalaz-releases" at "http://dl.bintray.com/scalaz/releases" // play-test -> specs2 -> scalaz-stream
  )
}

object Version {
  val play = _root_.play.core.PlayVersion.current

  val findbugs     = "2.0.1"
  val hsqldb       = "2.3.1"
  val javaxServlet = "3.0.1"
  val mockito      = "1.9.5"
  val reflections  = "0.9.8"
  val slick        = "2.1.0"
}

object Library {
  val findbugs     = "com.google.code.findbugs" %  "jsr305"            % Version.findbugs
  val hsqldb       = "org.hsqldb"               %  "hsqldb"            % Version.hsqldb
  val javaxServlet = "javax.servlet"            %  "javax.servlet-api" % Version.javaxServlet
  val mockito      = "org.mockito"              %  "mockito-all"       % Version.mockito
  val playJdbc     = "com.typesafe.play"        %% "play-jdbc"         % Version.play
  val playTest     = "com.typesafe.play"        %% "play-test"         % Version.play
  val reflections  = "org.reflections"          %  "reflections"       % Version.reflections
  val slick        = "com.typesafe.slick"       %% "slick"             % Version.slick
  val slickTestkit = "com.typesafe.slick"       %% "slick-testkit"     % Version.slick
}
