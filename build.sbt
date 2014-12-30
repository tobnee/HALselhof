name := "play-hal"

organization := "net.atinu"

version := "0.1"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.11.2", "2.10.4")

scalacOptions  ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers += "typesafe repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.7" % "provided"

libraryDependencies += "com.typesafe.play" %% "play" % "2.3.7" % "provided"

libraryDependencies += "org.scalatestplus" %% "play" % "1.1.0" % "test"
    