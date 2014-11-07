name := "play-slick-sample"

version := "1.0-SNAPSHOT"

lazy val root = Project("play-slick-sample", file("."))
  .enablePlugins(PlayScala)
  .dependsOn(ProjectRef(file("../../code"), "playSlick"))

scalaVersion := "2.10.4"

resolvers ++= DefaultOptions.resolvers(snapshot = true)

resolvers += Resolver.typesafeRepo("releases")

resolvers += "scalaz-releases" at "http://dl.bintray.com/scalaz/releases" // specs2 depends on scalaz-stream

libraryDependencies ++= Seq(
  jdbc
)

javaOptions in (Test, test) := Seq("-Xmx256m", "-XX:MaxPermSize=128M")
