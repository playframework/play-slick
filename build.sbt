name := "play-slick"

licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

homepage := Some(url("https://github.com/freekh/play-slick"))

version := "0.5.0.3-SNAPSHOT"

organization := "com.typesafe.play"

scalaVersion := "2.10.2"

resolvers += Classpaths.sbtPluginReleases

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

scalacOptions += "-feature"

scalacOptions += "-deprecation"

libraryDependencies ++= {
  val playVersion = "2.2.0"
  Seq(
    "com.typesafe.play" %% "play" % playVersion,
    "com.typesafe.play" %% "play-jdbc" % playVersion,
    "com.typesafe.slick" %% "slick" % "1.0.1",
    "javax.servlet" % "javax.servlet-api" % "3.0.1", //needed by org.reflections
    "com.google.code.findbugs" % "jsr305" % "2.0.1", //needed by org.reflections
    ("org.reflections" % "reflections" % "0.9.8" notTransitive())
      .exclude("com.google.guava", "guava") //provided by play
      .exclude("javassist", "javassist"), //provided by play
    "com.typesafe.play" %% "play-test" % playVersion % "test")
}

