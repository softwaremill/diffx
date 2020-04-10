import com.softwaremill.PublishTravis.publishTravisSettings
import sbtcrossproject.CrossPlugin.autoImport.CrossType
import sbtcrossproject.crossProject

val v2_12 = "2.12.8"
val v2_13 = "2.13.1"

val scalatestDependency = "org.scalatest" %% "scalatest" % "3.1.1"
val specs2Dependency = "org.specs2" %% "specs2-core" % "4.9.3"
val smlTaggingDependency = "com.softwaremill.common" %% "tagging" % "2.2.1"

lazy val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ acyclicSettings ++ Seq(
  organization := "com.softwaremill.diffx",
  scalaVersion := v2_12,
  scalafmtOnCompile := true,
  crossScalaVersions := Seq(v2_12, v2_13),
  libraryDependencies ++= Seq(compilerPlugin("com.softwaremill.neme" %% "neme-plugin" % "0.0.5")),
  scmInfo := Some(ScmInfo(url("https://github.com/softwaremill/diffx"), "git@github.com:softwaremill/diffx.git")),
  // sbt-release
  releaseCrossBuild := true
)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "diffx-core",
    libraryDependencies ++= Seq(
      "com.propensive" %% "magnolia" % "0.14.3",
      scalatestDependency % "test"
    ),
    unmanagedSourceDirectories in Compile += {
      // sourceDirectory returns a platform-scoped directory, e.g. /.jvm
      val sourceDir = (baseDirectory in Compile).value / ".." / "src" / "main"
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
        case _                       => sourceDir / "scala-2.13-"
      }
    }
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val scalatest = crossProject(JVMPlatform, JSPlatform)
  .in(file("scalatest"))
  .settings(commonSettings: _*)
  .settings(
    name := "diffx-scalatest",
    libraryDependencies ++= Seq(
      scalatestDependency
    )
  )
  .dependsOn(core)

lazy val scalatestJVM = scalatest.jvm
lazy val scalatestJS = scalatest.js

lazy val specs2 = crossProject(JVMPlatform, JSPlatform)
  .in(file("specs2"))
  .settings(commonSettings: _*)
  .settings(
    name := "diffx-specs2",
    libraryDependencies ++= Seq(
      specs2Dependency
    )
  )
  .dependsOn(core)

lazy val specs2JVM = specs2.jvm
lazy val specs2JS = specs2.js

lazy val utest = crossProject(JVMPlatform, JSPlatform)
  .in(file("utest"))
  .settings(commonSettings: _*)
  .settings(
    name := "diffx-utests",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % "0.7.4"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .dependsOn(core)

lazy val utestJVM = utest.jvm
lazy val utestJS = utest.js

lazy val tagging = crossProject(JVMPlatform, JSPlatform)
  .in(file("tagging"))
  .settings(commonSettings: _*)
  .settings(
    name := "diffx-tagging",
    libraryDependencies ++= Seq(
      smlTaggingDependency,
      scalatestDependency % "test"
    )
  )
  .dependsOn(core)

lazy val taggingJVM = tagging.jvm
lazy val taggingJS = tagging.js

lazy val cats = crossProject(JVMPlatform, JSPlatform)
  .in(file("cats"))
  .settings(commonSettings: _*)
  .settings(
    name := "diffx-cats",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.1.1",
      scalatestDependency % "test"
    )
  )
  .dependsOn(core)

lazy val catsJVM = cats.jvm
lazy val catsJS = cats.js

lazy val refined = crossProject(JVMPlatform, JSPlatform)
  .in(file("refined"))
  .settings(commonSettings: _*)
  .settings(
    name := "diffx-refined",
    libraryDependencies ++= Seq(
      "eu.timepit" %% "refined" % "0.9.13",
      scalatestDependency % "test"
    )
  )
  .dependsOn(core)

lazy val refinedJVM = refined.jvm
lazy val refinedJS = refined.js

lazy val rootProject = project
  .in(file("."))
  .settings(commonSettings: _*)
  .settings(publishArtifact := false, name := "diffx")
  .settings(publishTravisSettings)
  .aggregate(
    coreJVM,
    coreJS,
    scalatestJVM,
    scalatestJS,
    specs2JVM,
    specs2JS,
    utestJVM,
    utestJS,
    refinedJVM,
    refinedJS,
    taggingJVM,
    taggingJS,
    catsJVM,
    catsJS
  )
