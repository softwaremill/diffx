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
      compilerPlugin("io.tryp" % "splain" % "0.4.1" cross CrossVersion.patch),
"com.softwaremill.common" %% "tagging" % "2.2.1",
      "ai.x" %% "diff" % "2.0.1",
      "org.scalatest" %% "scalatest" % "3.0.7",
      "com.softwaremill" %% "magnolia" % "0.11.0-sml"
    ),
    scalacOptions += "-Xlog-implicits"

  )

