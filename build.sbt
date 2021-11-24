import CiCommands.{ ciBuild, devBuild }
import xerial.sbt.Sonatype.autoImport.sonatypeCredentialHost

scalaVersion := "2.13.6"
crossScalaVersions := Seq("2.12.10", "2.13.6")

inThisBuild(
  List(
    organization := "io.vangogiel",
    homepage := Some(url("http://vangogiel.io/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        id = "vangogiel",
        name = "Norbert Gogiel",
        email = "vangogiel@hotmail.co.uk",
        url = url("http://vangogiel.io")
      )
    ),
    sonatypeCredentialHost := "s01.oss.sonatype.org"
  )
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-unused:imports",
  "-Ywarn-dead-code",
  "-Xlint:adapted-args",
  "-Xsource:2.13",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.9.2",
  "com.typesafe.play" %% "play" % "2.8.8",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"
)

commands ++= Seq(ciBuild, devBuild)

coverageMinimumStmtTotal := 100
coverageFailOnMinimum := true
