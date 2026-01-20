package africa.shuwari.sbt

import sbt.Def
import sbt.Developer
import sbt.Keys.*
import sbt.ModuleID
import sbt.Project
import sbt.Setting
import sbt.url

object ShuwariCorePluginImports:

  extension (p: Project)
    def notPublished: Project = p.settings(Shuwari.notPublishedSettings)
    def shuwariProject: Project = p.settings(Shuwari.organisationSettings)
    def dependsOn(libraries: Def.Initialize[ModuleID]*): List[Setting[Seq[ModuleID]]] =
      libraries.toList.map(libraryDependencies += _.value)

  object Shuwari:

    val notPublishedSettings: List[Setting[?]] = List(
      publish / skip := true,
      publish := {},
      publishLocal := {},
      publishArtifact := false
    )
    val organisationSettings: List[Setting[?]] = List(
      organizationName := "Shuwari Africa Ltd.",
      organizationHomepage := Some(url("https://shuwari.africa")),
      developers := List(
        Developer(
          "shuwari-dev",
          "Shuwari Africa Ltd. Developer Team",
          "https://github.com/shuwariafrica",
          url("https://shuwari.africa/dev")
        )
      ),
      versionScheme := Some("semver-spec")
    )
