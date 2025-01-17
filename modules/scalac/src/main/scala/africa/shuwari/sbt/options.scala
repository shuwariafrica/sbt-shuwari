package africa.shuwari.sbt

import africa.shuwari.sbt.ScalacKeys.*
import org.typelevel.scalacoptions.{ScalaVersion, ScalacOption, ScalacOptions}
import sbt.*

/** Utility for configuring Scala compiler options across different build modes.
  */
object ScalaCompilerOptions {

  final val options = ScalacOptions

  /** Helper to restrict certain compiler options to Scala 3.
    */
  private def dottyOnly: ScalaVersion => Boolean = (v: ScalaVersion) => v.isAtLeast(ScalaVersion.V3_0_0)

  /** Enable checking of modifiers validity.
    */
  def checkMods: ScalacOption = options.privateOption("check-mods", dottyOnly)

  /** Enable checking for reentrant initializations.
    */
  def checkReentrant: ScalacOption = options.privateOption("check-reentrant", dottyOnly)

  /** Enable safer handling of nulls by distinguishing between `T` and `T | Null`.
    */
  def explicitNulls: ScalacOption = options.other("-explicit-nulls", dottyOnly)

  /** Require `@targetName` for enums in Scala 3.
    */
  def requireTargetName: ScalacOption = options.privateOption("require-targetName", dottyOnly)

  /** Limit the maximum number of inline calls allowed in Scala 3.
    */
  def maxInlines: ScalacOption = options.advancedOption("max-inlines:64", dottyOnly)

  /** Default set of compiler options for all builds (Development/Verbose mode).
    */
  def defaultOptions: Set[ScalacOption] =
    options.default ++ options.fatalWarningOptions ++ Set(
      options.explain, // Detailed error reports
      options.explainTypes, // Full type inference information,
      checkMods,
      checkReentrant,
      maxInlines,
      requireTargetName
    )

  /** Define stricter compiler options for CI builds, adding optimizer warnings.
    */
  def ciOptions(
    releaseOptionsKey: SettingKey[Set[ScalacOption]]
  ): Def.Initialize[Set[ScalacOption]] =
    Def.setting {
      // Add optimizer warnings only if basePackages is defined
      val optimizerWarnings: Set[ScalacOption] =
        if (basePackages.value.nonEmpty) Set(options.optimizerWarnings)
        else Set.empty[ScalacOption] // Explicitly define type here
      releaseOptionsKey.value ++ optimizerWarnings
    }

  /** Dynamically customize release options with respect to `basePackages` pattern.
    */
  def tpolecatReleaseOptionsSetting(
    developmentOptions: SettingKey[Set[ScalacOption]]
  ): Def.Initialize[Set[ScalacOption]] = Def.setting {
    val base = basePackages.value
    val devOpts = developmentOptions.value

    // Add optimizer options if `basePackages` is defined; otherwise keep default
    if (base.nonEmpty)
      devOpts ++ options.optimizerOptions(base*)
    else
      devOpts
  }
}
