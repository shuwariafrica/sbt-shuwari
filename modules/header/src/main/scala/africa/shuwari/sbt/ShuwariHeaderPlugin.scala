package africa.shuwari.sbt

import sbt.Def
import sbt.Keys.*
import sbt.URL
import sbt.*
import sbtheader.CommentCreator
import sbtheader.CommentStyle
import sbtheader.FileType
import sbtheader.HeaderPlugin
import sbtheader.HeaderPlugin.autoImport.HeaderPattern
import sbtheader.HeaderPlugin.autoImport.*
import sbtheader.License
import sbtheader.LicenseDetection

import africa.shuwari.sbt.ShuwariHeaderImports.*

import scala.language.implicitConversions

object ShuwariHeaderPlugin extends AutoPlugin:
  val autoImport = ShuwariHeaderImports
  override def requires: Plugins = ShuwariCorePlugin && HeaderPlugin

  override def trigger: PluginTrigger = allRequirements

  override def buildSettings: Seq[Def.Setting[?]] = List(
    headerCopyrightHolder := None
  )

  override def projectSettings: Seq[Setting[?]] = Seq(
    headerMappings := {
      val scalaCommentStyleTypes =
        List(FileType.java, FileType.scala)
          .map(_ -> scalaBlockCommentStyle)
      headerMappings.value ++ scalaCommentStyleTypes
    },
    headerEmptyLine := false,
    headerLicense := defaultHeaderLicense.value
  )

  final private case class DefaultBlockCommentCreator(
    commentPrefix: String,
    commentSuffix: Option[String],
    linePrefix: String,
    lineSuffix: String,
    boundaryCharacter: String,
    postIndent: Boolean,
    indentSize: Int,
    preLengthModifier: Int,
    postLengthModifier: Int
  ) extends CommentCreator:

    override def apply(text: String, existingText: Option[String]): String =
      val longestLineSize = text.linesIterator.map(_.length).max

      val maxLength =
        longestLineSize + linePrefix.length + lineSuffix.length + indentSize + 1

      def padding(count: Int) = " " * count

      val indent = padding(indentSize)

      def processLines(line: String) =
        (line, linePrefix, lineSuffix) match
          case (str, pre, post) =>
            def first = s"$indent$pre $str"
            s"$first${padding(maxLength - (first.length + post.length))} $post"

      def firstLine =
        s"$commentPrefix${boundaryCharacter * (preLengthModifier + maxLength - commentPrefix.length)}"

      def lastLine =
        s"${if postIndent then indent else ""}${commentSuffix
            .map(str => (boundaryCharacter * (postLengthModifier + maxLength - (str.length + indent.length))) + str)
            .getOrElse(firstLine)}"

      (firstLine +: text.linesIterator.toList.map(processLines) :+ lastLine)
        .mkString(System.lineSeparator)

  private val scalaBlockCommentStyle: CommentStyle = CommentStyle(
    DefaultBlockCommentCreator(
      raw"/*",
      Some(raw"*/"),
      "*",
      "*",
      "*",
      postIndent = true,
      1,
      1,
      2
    ),
    HeaderPattern.commentBetween("""/\*+""", "*", """\*/""")
  )

  private def defaultHeaderLicense: Def.Initialize[Option[License]] =
    val apacheLicensePattern =
      """(?i)\bapache[- ]license[- ]2\.0|apache[- ]2\.0|apache-2\.0|apache\s*2\.0|apache-2\.0\b""".r
    val gplv3Pattern =
      """(?i)\bgpl[- ]v3|gnu[- ]gpl[- ]v3|gnu[- ]general[- ]public[- ]license[- ]v3|gpl[- ]3\.0|gpl[- ]3|gnu[- ]gpl[- ]3\.0|gnu[- ]gpl[- ]3|gpl-3\.0\b""".r

    val mitPattern = """(?i)\bmit\b""".r

    Def.setting {
      val licensesList = licenses.value.toList

      val matchedLicense = licensesList.collectFirst {
        case l if apacheLicensePattern.findFirstIn(l.spdxId).isDefined => Headers.apacheLicenseHeader.value
        case l if gplv3Pattern.findFirstIn(l.spdxId).isDefined         => Headers.gplv3LicenseHeader.value
        case l if mitPattern.findFirstIn(l.spdxId).isDefined           => Headers.mitLicenseHeader.value
      }

      matchedLicense.orElse(
        if licensesList.isEmpty then Some(Headers.internalSoftwareHeader.value)
        else
          LicenseDetection(
            licensesList,
            organizationName.value,
            startYear.value,
            headerEndYear.value,
            headerLicenseStyle.value
          )
      )
    }

  object Headers:

    private inline def developmentTimeline =
      import java.time.Year
      val start = startYear.value.get
      val current: Int = Year.now.getValue
      (start, current) match
        case (s, c) if s > c =>
          throw new RuntimeException(
            "Invalid `startYear` or system date value. Start year cannot be after the current year."
          ) // scalafix:ok
        case (s, c) if s < c => s"$s, $c"
        case (_, c)          => c

    private inline def end(str: String): String =
      if str.endsWith(".") then str else str + "."

    def apacheLicenseHeader: Def.Initialize[License.Custom] =
      Def.setting {
        def copyRightHolder = headerCopyrightHolder.value.getOrElse(Keys.organizationName.value)
        License.Custom(
          s"""|Copyright © $developmentTimeline ${end(copyRightHolder)}
              |
              |This file is licensed to you under the terms of the Apache
              |License Version 2.0 (the "License"); you may not use this
              |file except in compliance with the License. You may obtain
              |a copy of the License at:
              |
              |    https://www.apache.org/licenses/LICENSE-2.0
              |
              |Unless required by applicable law or agreed to in writing,
              |software distributed under the License is distributed on an
              |"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
              |either express or implied. See the License for the specific
              |language governing permissions and limitations under the
              |License.
              |""".stripMargin
        )
      }

    def gplv3LicenseHeader: Def.Initialize[License.Custom] =
      Def.setting {
        def copyRightHolder = headerCopyrightHolder.value.getOrElse(Keys.organizationName.value)
        License.Custom(
          s"""|Copyright © ${end(copyRightHolder)}
              |
              |This software is licensed to you under the terms of the GNU
              |General Public License, as published by the Free Software
              |Foundation; you may not use this file except in compliance
              |with either version 3 of the License, or (at your option) any
              |later version. You should have received a copy of the GNU
              |General Public License along with this software. You may
              |obtain a copy of the License at:
              |
              |    https://www.gnu.org/licenses/gpl-3.0.en.html
              |
              |Unless required by applicable law or agreed to in writing,
              |software distributed under the License is distributed on an
              |"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
              |either express or implied. See the License for the specific
              |language governing permissions and limitations under the
              |License.
              |""".stripMargin
        )
      }

    def mitLicenseHeader: Def.Initialize[License.Custom] =
      Def.setting {
        def copyRightHolder = headerCopyrightHolder.value.getOrElse(Keys.organizationName.value)
        License.Custom(
          s"""|Copyright © ${end(copyRightHolder)}
              |
              |Permission is hereby granted, free of charge, to any person
              |obtaining a copy of this software and associated
              |documentation files (the "Software"), to deal in the
              |Software without restriction, including without limitation
              |the rights to use, copy, modify, merge, publish, distribute,
              |sublicense, and/or sell copies of the Software, and to permit
              |persons to whom the Software is furnished to do so, subject
              |to the following conditions:
              |
              |The above copyright notice and this permission notice shall
              |be included in all copies or substantial portions of the
              |Software.
              |
              |THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
              |KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
              |WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
              |PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
              |OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
              |OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
              |OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
              |SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
              |""".stripMargin
        )
      }

    def internalSoftwareHeader: Def.Initialize[License.Custom] =
      Def.setting {
        def organizationName = headerCopyrightHolder.value.getOrElse(Keys.organizationName.value)
        License.Custom(
          s"""|Copyright © ${end(organizationName)} All rights reserved.
              |
              |This work is the sole property of $organizationName,
              |for internal use by ${organizationName}; and may not be
              |copied, used, and/or distributed without the express
              |permission of ${end(organizationName)}
              |""".stripMargin
        )
      }
