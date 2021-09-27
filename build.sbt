import CiCommands.{ ciBuild, devBuild }

scalaVersion := "2.13.0"
crossScalaVersions := Seq("2.12.10", "2.13.0")

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
    )
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

coverageMinimum := 100
coverageFailOnMinimum := true
