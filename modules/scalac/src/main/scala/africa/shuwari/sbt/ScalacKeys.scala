package africa.shuwari.sbt

import org.typelevel.scalacoptions.ScalacOption

import sbt.*

object ScalacKeys {

  val compilerOptions = settingKey[Set[ScalacOption]]("Set specifying ScalacOptions to be used.")

  val basePackages = settingKey[Set[String]](
    "Base package names for enabling, and limiting inlining to the specified pattern."
  )

}
