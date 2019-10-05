import com.softwaremill.PublishTravis.publishTravisSettings

val v2_12 = "2.12.8"
val v2_13_0 = "2.13.0"
val v2_13_1 = "2.13.1"

val scalatestDependency = "org.scalatest" %% "scalatest" % "3.0.8"

lazy val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ acyclicSettings ++ Seq(
  organization := "com.softwaremill.diffx",
  scalaVersion := v2_12,
  scalafmtOnCompile := true,
  crossScalaVersions := Seq(v2_12, v2_13_0, v2_13_1),
  libraryDependencies ++= Seq(compilerPlugin("com.softwaremill.neme" %% "neme-plugin" % "0.0.4")),
)

lazy val core: Project = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "diffx-core",
    libraryDependencies ++= Seq(
      "com.propensive" %% "magnolia" % "0.12.0",
      scalatestDependency % "test",
    ),
    unmanagedSourceDirectories in Compile += (baseDirectory in Compile).value / "src" / "main" / "scala-common",
    unmanagedSourceDirectories in Compile += {
      // sourceDirectory returns a platform-scoped directory, e.g. /.jvm
      val sourceDir = (baseDirectory in Compile).value / "src" / "main"
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
        case _                       => sourceDir / "scala-2.13-"
      }
    }
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
