# sbt-shuwari

Collection of [sbt](https://scala-sbt.org) plugins for uniform configuration of Shuwari Africa Ltd. sbt projects, as well
as CI and Release related functionality.

May also be used for other projects if the default settings are found useful.
Any issues may be reported to the project [issue tracker](https://dev.azure.com/shuwari/sbt-shuwari/_workitems/create/issue).

[![Published Version](https://maven-badges.herokuapp.com/maven-central/africa.shuwari.sbt/sbt-shuwari/badge.svg)](https://maven-badges.herokuapp.com/maven-central/africa.shuwari.sbt/sbt-shuwari/)
[![Build Status](https://github.com/unganisha/sbt-shuwari/actions/workflows/build.yml/badge.svg)](https://github.com/unganisha/sbt-shuwari/actions/workflows/build.yml)
[![Board Status](https://dev.azure.com/shuwari/79d8b623-e785-4397-8c14-0a0b3645f461/eaa58a91-e40a-46a5-b8f7-cfa30dbece27/_apis/work/boardbadge/bc91e17a-5d52-4d3a-aec3-e9a2678b1a10?columnOptions=1)](https://dev.azure.com/shuwari/79d8b623-e785-4397-8c14-0a0b3645f461/_boards/board/t/eaa58a91-e40a-46a5-b8f7-cfa30dbece27/Microsoft.RequirementCategory/)
__________________________________

_NB: Unless specified otherwise, all plugins listed below are sbt `AutoPlugins`, and will be enabled automatically upon enabling the required plugin dependencies for each._

## Core Plugins

All core plugins listed below may be included in the project separately, or otherwise collectively using the following coordinates:

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari" % "0.11.0")
```

|Includes:                                                                                                                                                             |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
|[ShuwariCorePlugin](#shuwaricoreplugin), [ShuwariHeaderPlugin](#shuwariheaderplugin), [BuildModePlugin](#buildmodeplugin), [ScalacOptionsPlugin](#scalacoptionsplugin)|

**For individual plugin dependency coordinates, see below:**

### [ShuwariCorePlugin](modules/core/src/main/scala/africa/shuwari/sbt/plugin.scala)

Preconfigures projects with Shuwari Africa Ltd. project defaults.
  
Specifically, sets `ThisBuild / organizationHomepage`, `ThisBuild / organizationName`, and `ThisBuild / scmInfo` to the values specified for the root
project by default. For example, setting `scalaVersion` will also set the same value for `ThisBuild / scalaVersion`. _Note: No longer resolves circular dependencies caused by existing sbt defaults. the following settings must be set explicitly for the root project to avoid errors during project launch: `organizationHomepage`, `organizationName`_

Additionally, sets `organizationName`, `organizationHomepage`, `organization`, `apiURL`, `developers`, `homepage`, `licenses`, `startYear`, and `version`
for all non-root projects to the values specified in the the root project by default. For example, setting `version` for the root project will propagate
to all subprojects.

Provides convenience methods for specifying the following common settings. In all cases, the method is available either as a setting or collection of settings
directly usable as such, or as an extension method to `Project` of the same name.

- `shuwariProject`: Sets default Shuwari Africa Ltd. `organizationHomepage`, `organizationName`, and `developers` settings.
- `notPublished`: Disables publishing of artefacts for the specified project.

For example:

```scala
lazy val `amazing-project` =
  project
    .shuwariProject
    .notpublished
```

It may be resolved via the following coordinates:

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari-core" % "0.11.0")
```

### [ShuwariHeaderPlugin](modules/header/src/main/scala/africa/shuwari/sbt/plugin.scala)

Preconfigures the excellent [sbt-header](https://github.com/sbt/sbt-header) plugin to apply standardised
Shuwari Africa source file headers dependent on whether the project is Open Source or otherwie.

The selection of which license header is applied is dependent on the `sbt.Keys.licences` setting value. Where
`licenses` is empty, the default internal use header shall be applied; otherwise if `licenses` contains a single
member, denoting the Apache 2.0 License, then the default Apache 2.0 License header shall be applied instead.

It may be resolved via the following coordinates:

```scala
addSbtPlugin("africa.shuwari.sbt" %  "sbt-shuwari-header" % "0.11.0")
```

Additionally, a project's license may be specified explicitly by including either `internalSoftware`, or `apacheLicensed`
in the project's settings definition.

For example:

```scala
lazy val `amazing-project` =
  project
    .settings(apacheLicensed)

// Convenience extension methods similarly named have also been provided to allow the same with less boilerplate code.
lazy val `amazing-project` =
  project
    .internalSoftware
```

### [BuildModePlugin](modules/mode/src/main/scala/africa/shuwari/sbt/plugin.scala)

Provides a build scoped `buildMode` sbt `SettingKey` allowing the specification of separate settings dependent on
current build environment; specifically one of `Mode.Development`, `Mode.Integration`, or `Mode.Release`.

`buildMode` can be configured by being set explicitly, for example in your `build.sbt` file:

```scala
ThisBuild / buildMode := Mode.Integration
```

Alternatively, the environment variable `BUILD_MODE` may be used with one of the following values to select the respective
modes: `DEVELOPMENT`, `INTEGRATION`, or `RELEASE`.

It may be resolved via the following coordinates:

```scala
addSbtPlugin("africa.shuwari.sbt" %  "sbt-shuwari-mode" % "0.11.0")
```

### [ScalaOptionsPlugin](modules/scalac/src/main/scala/africa/shuwari/sbt/plugin.scala)

Provides a set of default scalac compiler options based on the active  `buildMode`. Uses the excellent [sbt-tpolecat](https://github.com/typelevel/sbt-tpolecat)
under the hood.

|Depends On:                                                                                   |
| -------------------------------------------------------------------------------------------- |
|[BuildModePlugin](#buildmodeplugin), [sbt-tpolecat](https://github.com/typelevel/sbt-tpolecat)|

Introduces one new setting, `basePackages`, defining the package under which files to be optimised under production mode compilation are contained.

## Supplementary Plugins

### [ShuwariJsPlugin](modules/js/src/main/scala/africa/shuwari/plugin.scala)

Provides a set of default ScalaJS linker options based on the active  `buildMode` or specified `basePackage`.

|Depends On:                                                                                                                   |
| ---------------------------------------------------------------------------------------------------------------------------- |
| [BuildModePlugin](#buildmodeplugin), [ScalacOptionsPlugin](#scalacoptionsplugin), [ScalaJSPlugin](https://www.scala-js.org/) |

- Configures project to emit [ES Modules](https://www.scala-js.org/doc/project/module.html) by default.
- If `basePackage` is configures, configures module [split style](https://www.scala-js.org/api/scalajs-linker-interface-js/latest/org/scalajs/linker/interface/ModuleSplitStyle$.html)
  to produce the [smallest modules](https://www.scala-js.org/api/scalajs-linker-interface-js/latest/org/scalajs/linker/interface/ModuleSplitStyle$$SmallModulesFor.html)
  for the specified package, and subpackages. Otherwise uses [`FewestModules`](https://www.scala-js.org/api/scalajs-linker-interface-js/latest/org/scalajs/linker/interface/ModuleSplitStyle$$FewestModules$.html) for when `BuildMode` is set to `BuildMode.Development`, and [`SmallestModules`](https://www.scala-js.org/api/scalajs-linker-interface-js/latest/org/scalajs/linker/interface/ModuleSplitStyle$$SmallestModules$.html) otherwise.

__________________________________

## License

Copyright © Shuwari Africa Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License at:

  [`http://www.apache.org/licenses/LICENSE-2.0`](https://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
