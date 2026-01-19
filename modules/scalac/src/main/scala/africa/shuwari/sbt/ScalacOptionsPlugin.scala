package africa.shuwari.sbt

import sbt.Keys.scalaVersion
import sbt._

import africa.shuwari.sbt.ScalacOptionsPluginImport._

/** Plugin to integrate build modes and compiler options into sbt projects. */
object ScalacOptionsPlugin extends AutoPlugin:

  val autoImport: ScalacOptionsPluginImport.type = ScalacOptionsPluginImport

  override def requires: Plugins = BuildModePlugin
  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[?]] = Seq(
    basePackages := Set.empty,
    compilerOptions :=
      ScalacOptions.effectiveOptions(scalaVersion.value, BuildModePlugin.buildMode.value, basePackages.value),
    Test / compilerOptions := (Compile / compilerOptions).value.exclude(ScalacOptions.explicitNulls,
                                                                        ScalacOptions.languageStrictEquality),
    Compile / Keys.compile / Keys.scalacOptions := Def.settingDyn {
      val options = compilerOptions.value
      Def.setting(options.scalac)
    }.value,
    Test / Keys.compile / Keys.scalacOptions := Def.settingDyn {
      val options = (Test / compilerOptions).value
      Def.setting(options.scalac)
    }.value
  )
