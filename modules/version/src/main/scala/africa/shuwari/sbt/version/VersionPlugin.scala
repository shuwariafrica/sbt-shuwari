package africa.shuwari.sbt.version

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import sbt.Keys.*
import sbt.*
import sbtdynver.DynVerPlugin
import sbtdynver.DynVerPlugin.autoImport.*

object VersionPlugin extends AutoPlugin {

  override def requires: Plugins = DynVerPlugin
  override def trigger = allRequirements

  object autoImport {
    val fullVersion = settingKey[String]("Full version, including attached metadata of the project.")
  }

  override def projectSettings: Seq[Def.Setting[?]] = Seq(
    version := versionSetting.value,
    dynver := versionSetting.toTaskable.toTask.value,
    autoImport.fullVersion := implementationVersionSetting.value
  )

  def manifestAttributes: Def.Initialize[Map[String, String]] = Def.setting {
    def buildDate = ZonedDateTime.now(ZoneId.of("Z")).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
    Map(
      "Implementation-Title" -> name.value,
      "Implementation-Version" -> autoImport.fullVersion.value,
      "Implementation-Vendor" -> organizationName.value,
      "Specification-Version" -> version.value,
      "Build-Jdk" -> sys.props("java.version"),
      "Build-Jdk-Vendor" -> sys.props("java.vendor"),
      "Build-Tool" -> s"sbt ${sbtVersion.value}",
      "Build-Scala-Version" -> scalaVersion.value,
      "Built-By" -> sys.props("user.name"),
      "Build-Date" -> buildDate
    )
  }

  def manifestAttributes(attributes: Map[String, String]): Def.Initialize[Map[String, String]] =
    Def.setting(manifestAttributes.value ++ attributes)

  private def baseVersionSetting(appendMetadata: Boolean): Def.Initialize[String] = Def.setting {
    dynverGitDescribeOutput.value.mkVersion(
      (in: sbtdynver.GitDescribeOutput) => {
        val meta = if (appendMetadata) s"+${in.commitSuffix.distance}.${in.commitSuffix.sha}" else ""
        if (!in.isSnapshot()) in.ref.dropPrefix
        else {
          val parts = in.ref.dropPrefix.split("\\.").map(_.toInt)
          val lastIndex = parts.length - 1
          val incrementedParts = parts.updated(lastIndex, parts(lastIndex) + 1).map(_.toString)
          s"${incrementedParts.mkString(".")}-SNAPSHOT$meta"
        }
      },
      "SNAPSHOT"
    )
  }

  private def versionSetting = baseVersionSetting(appendMetadata = false)
  private def implementationVersionSetting = baseVersionSetting(appendMetadata = true)
}
