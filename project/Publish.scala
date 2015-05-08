import sbt._
import sbt.Keys._

object Publish {
  val settings = Seq(
    homepage := Some(url("https://github.com/playframework/play-slick")),
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),

    pomExtra := {
      <scm>
        <url>git@github.com:playframework/play-slick.git</url>
        <connection>scm:git:git@github.com:playframework/play-slick.git</connection>
      </scm>
      <developers>
        <developer>
          <id>playframework</id>
          <name>Play Framework Team</name>
          <url>https://github.com/playframework</url>
        </developer>
      </developers>
    },

    pomIncludeRepository := { _ => false }
  )
}
