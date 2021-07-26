import sbt.Command

object CiCommands {
  def ciBuild: Command = Command.command("ciBuild") { state ⇒
    "clean" :: "scalafmtSbtCheck" :: "scalafmtCheck" ::
      "test:scalafmtCheck" :: "test" ::
      state
  }

  def devBuild: Command = Command.command("devBuild") { state ⇒
    "clean" :: "scalafmtSbt" :: "scalafmt" :: "test:scalafmt" ::
      "test" ::
      state
  }
}
