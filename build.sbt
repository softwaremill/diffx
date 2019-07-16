lazy val commonSettings = commonSmlBuildSettings ++ acyclicSettings ++ Seq(
  organization := "com.softwaremill.diffx",
  scalaVersion := "2.12.8",
  scalafmtOnCompile := true
)

val scalatestDependency = "org.scalatest" %% "scalatest" % "3.0.8"

lazy val rootProject = (project in file("."))
  .settings(name := "diffx")
  .settings(commonSettings)
  .aggregate(
    core,
    scalatest
  )

lazy val core: Project = (project in file("core"))
  .settings(commonSettings)
  .settings(
    name := "diffx-core",
    libraryDependencies ++= Seq(
      "com.propensive" %% "magnolia" % "0.11.0",
      scalatestDependency % "test",
    )
  )

lazy val scalatest: Project = (project in file("scalatest"))
  .settings(commonSettings)
  .settings(
    name := "diffx-scalatest",
    libraryDependencies ++= Seq(
      scalatestDependency
    )
  )
  .dependsOn(core)
