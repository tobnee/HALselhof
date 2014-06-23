name := "play-hal"

version := "1.0"

scalaVersion := "2.11.1"

resolvers += "typesafe repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"
    