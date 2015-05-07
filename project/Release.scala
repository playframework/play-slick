import sbt._
import sbt.Keys._
import sbt.complete.Parser
import sbtrelease._
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleaseStateTransformations._
import com.typesafe.sbt.pgp.PgpKeys
import xerial.sbt.Sonatype

object Release {
  lazy val settings = releaseSettings ++ Seq(
    Sonatype.autoImport.sonatypeProfileName := "com.typeasfe",
    ReleaseKeys.crossBuild := true,
    ReleaseKeys.publishArtifactsAction := PgpKeys.publishSigned.value,
    ReleaseKeys.releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      runTestIn("docs"),
      runTestIn("samples"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      ReleaseStep(action = { state =>
        Parser.parse("", Sonatype.SonatypeCommand.sonatypeRelease.parser(state)) match {
          case Right(command) => command()
          case Left(msg) => throw sys.error(s"Bad input for release command: $msg")
        }
      }),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

  def runTestIn(projectName: String) = ReleaseStep(
    action = { state: State =>
      if (!state.get(ReleaseKeys.skipTests).getOrElse(false)) {
        val extracted = Project.extract(state)
        val ref = LocalProject(projectName)
        extracted.runAggregated(test in Test in ref, state)
      } else state
    },
    enableCrossBuild = true
  )
}
