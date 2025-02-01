package africa.shuwari.sbt

import org.scalajs.linker.interface.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.typelevel.scalacoptions.ScalacOption
import org.typelevel.scalacoptions.ScalacOptions

import sbt.*

import africa.shuwari.sbt.ScalacKeys.compilerOptions

object JSPlugin extends AutoPlugin {

  override def requires: Plugins = ScalaJSPlugin && ScalacOptionsPlugin

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Def.Setting[?]] = {
    import ScalaJSPlugin.autoImport.*

    Seq(
      scalaJSLinkerConfig := defaultLinkerConfigOptions.value,
      compilerOptions := compilerOptions.value.diff(excludeJSIncompatibleOptions),
      (Test / compilerOptions) := (Test / compilerOptions).value.diff(excludeJSIncompatibleOptions)
    )
  }

  /** Default Scala.js linker configuration */
  def defaultLinkerConfigOptions: Def.Initialize[StandardConfig] = Def.setting {
    val basePackages = ScalacKeys.basePackages.value

    val splitStyle = BuildModePlugin.buildMode.value match {
      case BuildModePlugin.Mode.Development =>
        if (basePackages.nonEmpty) ModuleSplitStyle.SmallModulesFor(basePackages.toList)
        else ModuleSplitStyle.FewestModules
      case _ =>
        if (basePackages.nonEmpty) ModuleSplitStyle.SmallModulesFor(basePackages.toList)
        else ModuleSplitStyle.SmallestModules
    }

    val baseConfig = StandardConfig()
      .withModuleKind(ModuleKind.ESModule) // Use ES Modules
      .withESFeatures(ESFeatures.Defaults) // Default ES features
      .withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs")) // Use `.mjs` as output extension
      .withModuleSplitStyle(splitStyle) // Select module split style

    // Apply adjusted linker configuration dynamically based on build mode
    adjustedLinkerConfig(BuildModePlugin.buildMode.value, baseConfig)
  }

  /** Scala.js-incompatible scalac options to be excluded */
  private def excludeJSIncompatibleOptions: Set[ScalacOption] = Set(
    ScalaCompilerOptions.explicitNulls, // Explicit nulls are not fully compatible with Scala.js
    ScalaCompilerOptions.checkMods, // Modifier checks are JVM-specific
    ScalaCompilerOptions.checkReentrant // Reentrant checks are JVM-specific
  ) ++ ScalacOptions.fatalWarningOptions // Too strict for typical JS workflows

  /** Optimization level based on build mode */
  private def adjustedLinkerConfig(buildMode: BuildModePlugin.Mode, config: StandardConfig): StandardConfig =
    buildMode match {
      case BuildModePlugin.Mode.Development =>
        config
          .withClosureCompiler(false) // No advanced optimizations in development mode
          .withBatchMode(false) // Faster incremental optimization

      case _ => // Assume Integration or Production mode for full optimizations
        config
          .withClosureCompiler(true) // Enable Closure Compiler for full optimization
          .withBatchMode(true) // Best performance for full builds
    }

}
