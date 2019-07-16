lazy val commonSettings = commonSmlBuildSettings ++ acyclicSettings ++ Seq(
  organization := "com.sakiewka",
  scalaVersion := "2.12.8",
  scalafmtOnCompile := true
)

val circeVersion = "0.11.1"
val tapirVersion = "0.7"

lazy val core: Project = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "com.propensive" %% "magnolia" % "0.11.0",
      "org.scalatest" %% "scalatest" % "3.0.7" % "test",
    )
  )

lazy val scalatest: Project = (project in file("scalatest"))
  .settings(commonSettings: _*)
  .settings(
    name := "scalatest",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.7",
    )
  )
  .dependsOn(core)

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .aggregate(
    core,
    scalatest
  )
