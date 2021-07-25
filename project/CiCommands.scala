import sbt.Command

object CiCommands {
  def ciBuild: Command = Command.command("ciBuild") { state ⇒
    "clean" :: "scalafmtSbtCheck" :: "test" ::
      state
  }

  def devBuild: Command = Command.command("devBuild") { state ⇒
    "clean" :: "scalafmtSbt" :: "test" ::
      state
  }
}
