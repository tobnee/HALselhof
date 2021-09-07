import CiCommands.{ ciBuild, devBuild }
import com.jsuereth.sbtpgp.PgpKeys.gpgCommand

organization := "io.vangogiel"
name := "halselhof"
version := "0.4"

scalaVersion := "2.12.10"
crossScalaVersions := Seq("2.11.8", "2.12.10")

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-unused-import",
  "-Ywarn-dead-code",
  "-Yno-adapted-args",
  "-Xfuture",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.3" % "provided",
  "com.typesafe.play" %% "play" % "2.6.3" % "provided",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"
)

commands ++= Seq(ciBuild, devBuild)

coverageMinimum := 100
coverageFailOnMinimum := true

Global / gpgCommand := (baseDirectory.value / ".." / "gpg.sh").getAbsolutePath

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "s01.oss.sonatype.org",
  sys.env.getOrElse("SONATYPE_NEXUS_USERNAME", ""),
  sys.env.getOrElse("SONATYPE_NEXUS_PASSWORD", "")
)

ThisBuild / organization := "io.vangogiel.halselhof"
ThisBuild / organizationName := "vangogiel"
ThisBuild / organizationHomepage := Some(url("http://vangogiel.io/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/vangogiel/halselhof"),
    "scm:git@github.com:vangogiel/halselhof.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "vangogiel",
    name = "Norbert Gogiel",
    email = "vangogiel@hotmail.co.uk",
    url = url("http://vangogiel.io")
  )
)

ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/vangogiel/halselhof"))
ThisBuild / pomIncludeRepository := { _ =>
  false
}
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
