import com.softwaremill.PublishTravis.publishTravisSettings

val v2_12 = "2.12.8"
val v2_13 = "2.13.0"

val scalatestDependency = "org.scalatest" %% "scalatest" % "3.0.8"

lazy val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ acyclicSettings ++ Seq(
  organization := "com.softwaremill.diffx",
  scalaVersion := v2_12,
  scalafmtOnCompile := true,
  crossScalaVersions := Seq(v2_12, v2_13),
)

lazy val core: Project = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "diffx-core",
    libraryDependencies ++= Seq(
      "com.propensive" %% "magnolia" % "0.11.0",
      scalatestDependency % "test",
    )
  )

lazy val scalatest: Project = (project in file("scalatest"))
  .settings(commonSettings: _*)
  .settings(
    name := "diffx-scalatest",
    libraryDependencies ++= Seq(
      scalatestDependency,
    )
  )
  .dependsOn(core)

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publishArtifact := false, name := "diffx")
  .settings(publishTravisSettings)
  .aggregate(
    core,
    scalatest
  )
