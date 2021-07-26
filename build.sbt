import CiCommands.{ ciBuild, devBuild }

organization := "io.vangogiel"
name := "halselhof"
version := "0.2"

scalaVersion := "2.12.10"
crossScalaVersions := Seq("2.11.8", "2.12.10")

scalacOptions ++= Seq(
  "-unchecked",
  "-feature",
  "-Yno-adapted-args",
  "-Xfuture"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.3" % "provided",
  "com.typesafe.play" %% "play" % "2.6.3" % "provided",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.1" % "test"
)

commands ++= Seq(ciBuild, devBuild)

licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
