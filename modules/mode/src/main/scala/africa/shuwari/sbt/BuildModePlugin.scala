package africa.shuwari.sbt

import sbt.Def
import sbt.*
import sbt.plugins.JvmPlugin

object BuildModePlugin extends AutoPlugin {

  sealed trait Mode extends Product with Serializable {
    def ===(mode: Mode): Boolean = this == mode // scalafix:ok
    override def toString: String = this.getClass.getSimpleName.dropWhile(_ == '$').toLowerCase // scalafix:ok
  }
  object Mode {
    case object Development extends Mode
    case object Integration extends Mode
    case object Release extends Mode
  }

  object autoImport {
    final val Mode = BuildModePlugin.Mode
    type Mode = BuildModePlugin.Mode
    final val ci = BuildModePlugin.ci
    final val buildMode = BuildModePlugin.buildMode
  }

  override def trigger: PluginTrigger = allRequirements
  override def requires: Plugins = JvmPlugin

  override def buildSettings: Seq[Setting[?]] = Seq(
    buildMode := buildModeResolver.value,
    ci := ciResolver.value
  )

  val buildMode = settingKey[Mode](
    "Defines the current BuildMode. Defaults to Mode.Development unless \"BUILD_MODE\" environment variable is detected " +
      "and set to either \"DEVELOPMENT\", \"INTEGRATION\", or \"RELEASE\"."
  )

  val ci: SettingKey[Boolean] =
    settingKey[Boolean]("Indicates whether the current build is running in a CI environment.")

  private def ciResolver: Def.Initialize[Boolean] = Def.setting(sys.env.exists { case (k, _) =>
    k.equalsIgnoreCase("CI") ||
    k.equalsIgnoreCase("GITHUB_ACTIONS") ||
    k.equalsIgnoreCase("TF_BUILD") || // Azure Pipelines
    k.equalsIgnoreCase("BITBUCKET_BUILD_NUMBER") || // Bitbucket Pipelines
    k.equalsIgnoreCase("TEAMCITY_VERSION") // TeamCity
  })

  private def buildModeResolver: Def.Initialize[Mode] = Def.setting {
    val ci: Boolean = BuildModePlugin.ci.value
    val environmentSetting: String = "BUILD_MODE"
    val modes = Set(Mode.Development, Mode.Integration, Mode.Release).map(m => m.toString -> m).toMap
    val buildMode = sys.env.get(environmentSetting).flatMap(modes.get).getOrElse(Mode.Development)
    if (ci && buildMode == Mode.Development) Mode.Integration else buildMode // scalafix:ok
  }

}
