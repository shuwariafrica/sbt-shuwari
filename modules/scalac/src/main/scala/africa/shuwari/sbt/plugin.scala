package africa.shuwari.sbt

import org.typelevel.sbt.tpolecat.{CiMode, OptionsMode, ReleaseMode, TpolecatPlugin, VerboseMode}
import sbt.{Def, *}

/** Plugin to integrate build modes and compiler options into sbt projects. */
object ScalacOptionsPlugin extends AutoPlugin {

  object autoImport {
    final val ScalaCompiler = ScalacKeys
    final val ScalacOption = org.typelevel.scalacoptions.ScalacOption
    type ScalacOption = org.typelevel.scalacoptions.ScalacOption
  }

  override def requires: Plugins = BuildModePlugin && TpolecatPlugin
  override def trigger: PluginTrigger = allRequirements

  /** Dynamically set the tpolecat mode (VerboseMode, CiMode, ReleaseMode) based on the `buildMode`. */
  private def tpolecatPluginModeSetting: Def.Initialize[OptionsMode] =
    Def.setting(BuildModePlugin.buildMode.value match {
      case BuildModePlugin.Mode.Development => VerboseMode
      case BuildModePlugin.Mode.Integration => CiMode
      case BuildModePlugin.Mode.Release     => ReleaseMode
    })

  override def projectSettings: Seq[Setting[?]] = Seq(
    ScalacKeys.basePackages := List.empty, // Initialize `basePackages` as empty by default
    TpolecatPlugin.autoImport.tpolecatOptionsMode := tpolecatPluginModeSetting.value,

    // Default (Verbose) mode options
    TpolecatPlugin.autoImport.tpolecatDevModeOptions := ScalaCompilerOptions.defaultOptions,

    // CI mode options
    TpolecatPlugin.autoImport.tpolecatCiModeOptions := ScalaCompilerOptions
      .ciOptions(TpolecatPlugin.autoImport.tpolecatReleaseModeOptions)
      .value,

    // Release mode options
    TpolecatPlugin.autoImport.tpolecatReleaseModeOptions := ScalaCompilerOptions
      .tpolecatReleaseOptionsSetting(TpolecatPlugin.autoImport.tpolecatDevModeOptions)
      .value
  )
}
