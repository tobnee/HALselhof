name := "play-hal"

organization := "net.atinu"

version := "0.1.1"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8")

scalacOptions  ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers += "typesafe repo" at "http://repo.typesafe.com/typesafe/releases/"

scalariformSettings

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.10" % "provided"

libraryDependencies += "com.typesafe.play" %% "play" % "2.3.10" % "provided"

libraryDependencies += "org.scalatestplus" %% "play" % "1.2.0" % "test"
    