package africa.shuwari.sbt

import sbt.Keys.scalaVersion
import sbt.*

import africa.shuwari.sbt.BuildModePlugin.buildMode
import africa.shuwari.sbt.ScalaCompilerOptions.*
import africa.shuwari.sbt.ScalacKeys.basePackages
import africa.shuwari.sbt.ScalacKeys.compilerOptions

/** Plugin to integrate build modes and compiler options into sbt projects. */
object ScalacOptionsPlugin extends AutoPlugin {

  object autoImport {
    final val ScalaCompiler = ScalacKeys
    final val ScalacOption = org.typelevel.scalacoptions.ScalacOption
    type ScalacOption = org.typelevel.scalacoptions.ScalacOption
  }

  override def requires: Plugins = BuildModePlugin
  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[?]] = Seq(
    basePackages := Set.empty, // Initialize `basePackages` as empty by default,
    compilerOptions := effectiveOptions(scalaVersion.value, buildMode.value, basePackages.value),
    Test / compilerOptions := testOptions((Compile / compilerOptions).value),
    Compile / Keys.compile / Keys.scalacOptions := optionsList(compilerOptions.value),
    Test / Keys.compile / Keys.scalacOptions := optionsList((Test / compilerOptions).value)
  )
}
