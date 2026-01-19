package africa.shuwari.sbt

import sbt._
import sbt.plugins.JvmPlugin

object ShuwariCorePlugin extends AutoPlugin:
  override def requires: Plugins = JvmPlugin
  override def trigger: PluginTrigger = allRequirements
  val autoImport: ShuwariCorePluginImports.type = ShuwariCorePluginImports
