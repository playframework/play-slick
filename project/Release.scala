import sbt._
import sbt.Keys._
import sbtrelease._
import sbtrelease.ReleasePlugin._
import sbtrelease.ReleaseStateTransformations._
import com.typesafe.sbt.pgp.PgpKeys

object Release {
  lazy val settings = releaseSettings ++ Seq(
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
