package africa.shuwari.sbt

import sbt.Keys._
import sbt.Project
import sbt.librarymanagement.License
import sbt.settingKey
import sbt.url

object ShuwariHeaderImports:

  val headerCopyrightHolder =
    settingKey[Option[String]]("Name of the organisation or individual that holds the copyright.")

  inline def apacheLicensed = licenses := List(License.Apache2)
  inline def mitLicensed = licenses := List(License.MIT)
  inline def gplLicensed = licenses := List(License.GPL3_or_later)
  inline def internalSoftware = licenses := List.empty

  extension (p: Project)
    inline def license(l: License): Project = p.settings(licenses := List(l))
    inline def internalSoftware: Project = p.settings(ShuwariHeaderImports.internalSoftware)
