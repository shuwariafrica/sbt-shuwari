inThisBuild(
  List(
    scalaVersion := "3.8.0",
    organization := "africa.shuwari.sbt",
    organizationName := "Shuwari Africa Ltd.",
    organizationHomepage := Some(url("https://shuwari.africa/dev")),
    licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
    description := "Collection of sbt plugins for easy initialisation of uniform organisation wide default project settings.",
    homepage := Some(url("https://github.com/shuwariafrica/sbt-shuwari")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/unganisha/sbt-shuwari"),
        "scm:git@github.com:shuwariafrica/sbt-shuwari.git"
      )
    ),
    scalacOptions ++= List("-feature", "-deprecation"),
    startYear := Some(2022),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

val `sbt-shuwari-core` =
  project
    .in(modules("core"))
    .enablePlugins(SbtPlugin)
    .settings(publishSettings)

val `sbt-shuwari-mode` =
  project
    .in(modules("mode"))
    .enablePlugins(SbtPlugin)
    .settings(publishSettings)

val `sbt-shuwari-header` =
  project
    .in(modules("header"))
    .dependsOn(`sbt-shuwari-core`)
    .enablePlugins(SbtPlugin)
    .settings(addSbtPlugin("com.github.sbt" % "sbt-header" % "5.11.0"))
    .settings(publishSettings)

val `sbt-shuwari-scalac` =
  project
    .in(modules("scalac"))
    .enablePlugins(SbtPlugin)
    .dependsOn(`sbt-shuwari-core`)
    .dependsOn(`sbt-shuwari-mode`)
    .settings(libraryDependencies += "org.typelevel" %% "scalac-options" % "0.1.8")
    .settings(publishSettings)

val `sbt-shuwari` =
  project
    .in(file(".sbt-shuwari"))
    .dependsOn(`sbt-shuwari-core`, `sbt-shuwari-scalac`, `sbt-shuwari-header`)
    .enablePlugins(SbtPlugin)
    .settings(publishSettings)

// val `sbt-shuwari-js` =
//   project
//     .in(modules("js"))
//     .enablePlugins(SbtPlugin)
// //    .settings(publishSettings)
//     .dependsOn(`sbt-shuwari-mode`, `sbt-shuwari-scalac`)
//     .settings(addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.2"))

val `sbt-shuwari-documentation` =
  project
    .in(file(".sbt-shuwari-doc"))
    .dependsOn(`sbt-shuwari`)
    .enablePlugins(MdocPlugin)
    .settings(
      mdocIn := (LocalRootProject / baseDirectory).value / "modules" / "documentation",
      mdocOut := (LocalRootProject / baseDirectory).value,
      mdocVariables := Map(
        "VERSION" -> version.value
      )
    )

val `sbt-shuwari-build-root` =
  project
    .in(file("."))
    .enablePlugins(SbtPlugin)
    .settings(publish / skip := true)
    .aggregate(
      `sbt-shuwari-mode`,
      `sbt-shuwari-header`,
      `sbt-shuwari-scalac`,
      `sbt-shuwari-core`,
      `sbt-shuwari`
      // `sbt-shuwari-js`,
    )

def modules(name: String) = file(s"./modules/$name")

def publishSettings = pgpSettings ++: List(
  packageOptions += {
    val v = Version.Show.Extended.show(resolvedVersion.value)
    Package.ManifestAttributes(
      "Created-By" -> "Simple Build Tool",
      "Built-By" -> System.getProperty("user.name"),
      "Build-Jdk" -> System.getProperty("java.version"),
      "Specification-Title" -> name.value,
      "Specification-Version" -> version.value,
      "Specification-Vendor" -> organizationName.value,
      "Implementation-Title" -> name.value,
      "Implementation-Version" -> Version.Show.Extended.show(resolvedVersion.value),
      "Implementation-Vendor-Id" -> organization.value,
      "Implementation-Vendor" -> organizationName.value
    )
  },
  publishTo := {
    val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
    if (version.value.toLowerCase.contains("snapshot")) Some("central-snapshots".at(centralSnapshots))
    else localStaging.value
  },
  developers := List(
    Developer(
      id = "shuwaridev",
      name = "Shuwari Developer Team",
      email = "dev at shuwari africa",
      url = url("https://shuwari.africa/dev")
    )
  ),
  pomIncludeRepository := (_ => false),
  publishMavenStyle := true
)

def pgpSettings: List[Def.Setting[?]] = List(
  PgpKeys.pgpSelectPassphrase :=
    sys.props
      .get("SIGNING_KEY_PASSPHRASE")
      .map(_.toCharArray),
  usePgpKeyHex(System.getenv("SIGNING_KEY_ID"))
)
