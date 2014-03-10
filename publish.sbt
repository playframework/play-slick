publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>git@github.com:freekh/play-slick.git</url>
    <connection>scm:git:git@github.com:freekh/play-slick.git</connection>
  </scm>
  <developers>
    <developer>
      <id>freekh</id>
      <name>Fredrik Ekholdt</name>
      <url>http://ch.linkedin.com/in/freekh</url>
    </developer>
    <developer>
      <id>ms-tg</id>
      <name>Marc Siegel</name>
      <email>marc.siegel@timgroup.com</email>
      <url>https://github.com/ms-tg</url>
    </developer>
  </developers>
)

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else                             
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
