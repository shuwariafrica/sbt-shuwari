package africa.shuwari.sbt

import org.typelevel.scalacoptions.ScalaVersion
import org.typelevel.scalacoptions.ScalacOptionsDefinition

import scala.collection.immutable.ListSet

import sbt.librarymanagement.CrossVersion

import africa.shuwari.sbt.BuildModePlugin.Mode.Development

type ScalacOption = org.typelevel.scalacoptions.ScalacOption
val ScalacOption = org.typelevel.scalacoptions.ScalacOption

object ScalacOptions extends ScalacOptionsDefinition:
  private inline def atLeast37 = (v: ScalaVersion) => v.isAtLeast(ScalaVersion(3, 7, 0))

  val checkMods: ScalacOption = privateOption("check-mods", _.isAtLeast(ScalaVersion.V3_3_1))

  val checkMacros: ScalacOption = privateOption("check-macros", _.isAtLeast(ScalaVersion.V3_3_1))

  val checkReentrant: ScalacOption = privateOption("check-reentrant", _.isAtLeast(ScalaVersion.V3_3_1))

  val experimentalCaptureChecking: ScalacOption = languageFeatureOption("experimental.captureChecking", atLeast37)

  val experimentalErasedDefinitions: ScalacOption = languageFeatureOption("experimental.erasedDefinitions", atLeast37)

  val experimentalInto: ScalacOption = languageFeatureOption("experimental.into", atLeast37)

  val experimentalPureFunctions: ScalacOption = languageFeatureOption("experimental.pureFunctions", atLeast37)

  val experimentalSaferExceptions: ScalacOption = languageFeatureOption("experimental.saferExceptions", atLeast37)

  /** Enable safer handling of nulls by distinguishing between `T` and `T | Null`. */
  val explicitNulls: ScalacOption = privateOption("explicit-nulls", _.isAtLeast(ScalaVersion.V3_3_1))

  /** Require `@targetName` for enums in Scala 3. */
  val requireTargetName: ScalacOption = privateOption("require-targetName", _.isAtLeast(ScalaVersion.V3_3_1))

  /** Limit the maximum number of inline calls allowed in Scala 3. */
  val maxInlines: ScalacOption = maxInlines(64)

  override val default: ListSet[ScalacOption] = this.default ++ fatalWarningOptions ++ ListSet(
    explain,
    explainTypes,
    checkReentrant,
    newSyntax,
    explicitNulls,
    requireTargetName,
    maxInlines,
    languageStrictEquality
  )

  /** Returns the effective compiler options based on the Scala version and build mode.
    *
    * @param scalaVersion
    *   The Scala version.
    * @param mode
    *   The build mode.
    * @param basePackages
    *   Base packages for optimizer.
    * @return
    *   A set of compiler options.
    */
  private[sbt] inline def effectiveOptions(scalaVersion: String,
                                           mode: BuildModePlugin.Mode,
                                           basePackages: Set[String]): ListSet[ScalacOption] =
    supportedOptionsFor(
      scalaVersion,
      mode match
        case Development => default
        case _           =>
          if basePackages.nonEmpty then default ++ optimizerOptions(basePackages.toSeq*) + optimizerWarnings
          else default
    )

  private inline def supportedOptionsFor(
    version: String,
    modeScalacOptions: ListSet[ScalacOption]
  ): ListSet[ScalacOption] =
    (CrossVersion.partialVersion(version), version.split('.')) match
      case (Some((maj, min)), parts) =>
        val textMajOpt = parts.headOption.flatMap(_.toIntOption)
        val textMinOpt = parts.lift(1).flatMap(_.toIntOption)
        val patchNum = parts
          .lift(2)
          .map(_.takeWhile(_.isDigit))
          .flatMap(_.toIntOption)
          .getOrElse(0)
        val binaryVersion =
          if textMajOpt.contains(maj) && textMinOpt.contains(min)
          then ScalaVersion(maj, min, patchNum)
          else ScalaVersion(maj, min, 0)
        ListSet.from(ScalacOptions.optionsForVersion(binaryVersion, modeScalacOptions))
      case (None, _) => ListSet.empty[ScalacOption]

  private[sbt] inline def optionsList(v: ListSet[ScalacOption]): List[String] =
    v.flatMap(option => option.option :: option.args).toList
