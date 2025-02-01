package africa.shuwari.sbt

import org.typelevel.scalacoptions.ScalaVersion
import org.typelevel.scalacoptions.ScalacOption
import org.typelevel.scalacoptions.ScalacOptions

import scala.util.Try

import sbt.*

import africa.shuwari.sbt.BuildModePlugin.Mode.Development

/** Utility for configuring Scala compiler options across different build modes. */
object ScalaCompilerOptions {

  final val options = ScalacOptions

  /** Helper to restrict certain compiler options to Scala 3. */
  private val dottyOnly: ScalaVersion => Boolean = (v: ScalaVersion) => v.isAtLeast(ScalaVersion.V3_0_0)

  /** Enable checking of modifiers validity. */
  def checkMods: ScalacOption = options.privateOption("check-mods", dottyOnly)

  /** Enable checking for reentrant initializations. */
  def checkReentrant: ScalacOption = options.privateOption("check-reentrant", dottyOnly)

  /** Enable safer handling of nulls by distinguishing between `T` and `T | Null`. */
  def explicitNulls: ScalacOption = options.privateOption("explicit-nulls", dottyOnly)

  /** Require `@targetName` for enums in Scala 3. */
  def requireTargetName: ScalacOption = options.privateOption("require-targetName", dottyOnly)

  /** Limit the maximum number of inline calls allowed in Scala 3. */
  def maxInlines: ScalacOption = options.advancedOption("max-inlines:64", dottyOnly)

  /** Default compiler options */
  val defaultOptions: Set[ScalacOption] = options.default ++ options.fatalWarningOptions ++ Set(
    options.explain,
    options.explainTypes,
    checkMods,
    checkReentrant,
    explicitNulls,
    requireTargetName,
    maxInlines
  )

  /** Returns the effective compiler options based on the Scala version and build mode.
    * @param scalaVersion
    *   The Scala version.
    * @param mode
    *   The build mode.
    * @param basePackages
    *   Base packages for optimizer.
    * @return
    *   A set of compiler options.
    */
  def effectiveOptions(scalaVersion: String, mode: BuildModePlugin.Mode, basePackages: Set[String]): Set[ScalacOption] =
    supportedOptionsFor(
      scalaVersion,
      mode match {
        case Development => defaultOptions
        case _ =>
          if (basePackages.nonEmpty)
            defaultOptions ++ options.optimizerOptions(basePackages.toSeq*) + options.optimizerWarnings
          else defaultOptions
      }
    )

  /** Allows less strict compiler options for tests.
    * @param opts
    *   The original set of compiler options.
    * @return
    *   A filtered set of compiler options.
    */
  def testOptions(opts: Set[ScalacOption]): Set[ScalacOption] =
    opts.filterNot(o =>
      o.option == explicitNulls.option || options.fatalWarningOptions.exists(_.option == o.option)) // scalafix:ok

  /** Converts a set of compiler options to its corresponding list of strings.
    * @param v
    *   The set of compiler options.
    * @return
    *   A list of strings representing the compiler options.
    */
  def optionsList(v: Set[ScalacOption]): Seq[String] = v.flatMap(option => option.option :: option.args).toList

  // Copied from sbt-tpolecat (https://github.com/typelevel/sbt-tpolecat)
  private def supportedOptionsFor(
    version: String,
    modeScalacOptions: Set[ScalacOption]
  ): Set[ScalacOption] =
    (CrossVersion.partialVersion(version), version.split('.')) match {
      case (Some((maj, min)), Array(maj2, min2, patch))
          if maj.toString == maj2 && min.toString == min2 => // scalafix:ok
        val patchVersion = patch.takeWhile(_.isDigit)
        val binaryVersion = ScalaVersion(maj, min, Try(patchVersion.toLong).getOrElse(0))
        ScalacOptions.optionsForVersion(binaryVersion, modeScalacOptions)
      case (Some((maj, min)), _) =>
        val binaryVersion = ScalaVersion(maj, min, 0)
        ScalacOptions.optionsForVersion(binaryVersion, modeScalacOptions)
      case (None, _) =>
        Set.empty[ScalacOption]
    }
}
