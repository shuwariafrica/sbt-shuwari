# sbt-shuwari

A collection of [sbt](https://scala-sbt.org) plugins designed to provide unified configuration and extended build
functionality for Shuwari Africa Ltd. projects. It also includes CI/CD and release-related optimisations.

While optimised for internal use, these plugins can be used for other projects where their configurations are relevant.
Report any issues via the [issue tracker](https://dev.azure.com/shuwari/sbt-shuwari/_workitems/create/issue).

[![Maven Central](https://img.shields.io/maven-central/v/africa.shuwari.sbt/sbt-shuwari_2.12_1.0.svg)](https://maven-badges.herokuapp.com/maven-central/africa.shuwari.sbt/sbt-shuwari_2.12_1.0)
[![Build Status](https://github.com/unganisha/sbt-shuwari/actions/workflows/build.yml/badge.svg)](https://github.com/unganisha/sbt-shuwari/actions/workflows/build.yml)
[![Board Status](https://dev.azure.com/shuwari/79d8b623-e785-4397-8c14-0a0b3645f461/eaa58a91-e40a-46a5-b8f7-cfa30dbece27/_apis/work/boardbadge/bc91e17a-5d52-4d3a-aec3-e9a2678b1a10?columnOptions=1)](https://dev.azure.com/shuwari/79d8b623-e785-4397-8c14-0a0b3645f461/_boards/board/t/eaa58a91-e40a-46a5-b8f7-cfa30dbece27/Microsoft.RequirementCategory/)

---

## Table of Contents

1. [Introduction](#introduction)
2. [Usage](#usage)
3. [Core Plugins](#core-plugins)
    - ShuwariCorePlugin
    - ShuwariHeaderPlugin
    - BuildModePlugin
    - ScalacOptionsPlugin
4. [Supplementary Plugins](#supplementary-plugins)
    - ShuwariJsPlugin
5. [Licence](#licence)

---

## Introduction

`sbt-shuwari` is a suite of sbt plugins tailored to Shuwari Africa Ltd. projects. It offers:

- Centralised default settings for organising sbt builds.
- Advanced integration to manage build environments (Development, Integration, Release).
- Plugins for adding standardised headers to source files.
- Default configurations for scalac and Scala.js.

---

## Usage

To include all core plugins in your build, add the following to `plugins.sbt`:

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari" % "@VERSION@")
```

Individual plugins can also be added selectively by specifying their dependencies. See the specific plugin details
below.

---

## Core Plugins

All core plugins are **AutoPlugins**, meaning they are enabled automatically if their dependencies are met.

<details>
<summary>ShuwariCorePlugin</summary>

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari-core" % "@VERSION@")
```

### Features:

- Automatically sets common settings such as `organizationName`, `organizationHomepage`, and `versionScheme`.
- Includes developer information and other organisational settings for standardisation.
- Ensures projects can be omitted from publishing if required.

### Available Utilities & Settings

- **`shuwariProject`**:
    - Adds organisational defaults like `organizationName`, `organizationHomepage`, and `developers`.
    - Default settings:
      ```scala
      organizationName := "Shuwari Africa Ltd."
      organizationHomepage := Some(url("https://shuwari.africa"))
      versionScheme := Some("semver-spec")
      developers := List(
        Developer(
          "shuwari-dev",
          "Shuwari Africa Ltd. Developer Team",
          "developers at shuwari dot africa",
          url("https://shuwari.africa")
        )
      )
      ```
- **`notPublished`**:
    - Configures the project to skip all publishing tasks.
    - Default settings:
      ```scala
      publish / skip := true
      publish := {}
      publishLocal := {}
      publishArtifact := false
      ```
- **`dependsOn`**:
    - Simplified utility to depend on external libraries.

Example usage:

```scala
lazy val myProject = project
  .shuwariProject
  .notPublished
```

</details>

---

<details>
<summary>ShuwariHeaderPlugin</summary>

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari-header" % "@VERSION@")
```

### Features:

- Automatically detects the licensing model used in your project.
- Supports custom, Apache 2.0, or GPLv3-style licence headers.
- Includes configurable copyright holders.

### Available Configuration Keys

- **`headerCopyrightHolder`**:
    - Defines the entity owning the copyright.
    - Default: `None`.

- **`headerLicense`**:
    - The licence header included in source files.
    - Default: Automatically detects from the project configuration.
        - Defaults to an **internal licence** if no licence is specified.
        - Can be explicitly set to Apache 2.0 or GPLv3 headers.

Example:

```scala
lazy val project = (project in file("."))
  .settings(apacheLicensed)
```

</details>

---

<details>
<summary>BuildModePlugin</summary>

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari-mode" % "@VERSION@")
```

### Features:

- Dynamically resolves the `buildMode` from the `BUILD_MODE` environment variable.
- Configures different behaviours for development, integration, and release builds.

### Available Configuration Keys

- **`buildMode`**:
    - Configures the current build mode.
    - Default: Automatically detects from `BUILD_MODE`, defaults to `Development`.
      Valid values:
        - `"DEVELOPMENT"` → `Mode.Development`.
        - `"INTEGRATION"` → `Mode.Integration`.
        - `"RELEASE"` → `Mode.Release`.

Example:

```scala
ThisBuild / buildMode := Mode.Release
```

</details>

---

<details>
<summary>ScalacOptionsPlugin</summary>

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari-scalac" % "@VERSION@")
```

### Features:

- Seamlessly integrates with **`BuildModePlugin`** and **`TpolecatPlugin`**, enabling dynamic assignment of scalac
  options.
- Adjusts configurations for stricter linting and compilation optimisations based on the active build mode.
- **`tpolecatOptionsMode`**:
    - Dynamically maps `TpolecatPlugin` modes to the current **build mode**, as provided by `sbt-shuwari-mode`. This
      ensures tailored compiler flags for various scenarios:
        - `VerboseMode` corresponds to `Mode.Development`, promoting detailed feedback for debugging.
        - `CiMode` aligns with `Mode.Integration`, focusing on consistency and maintainability.
        - `ReleaseMode` associates with `Mode.Release`, ensuring production-ready builds.

  > For more details, refer to the section on [sbt-shuwari-mode](#).

### Available Configuration Keys:

- **`basePackages`**:
    - A list of base package names used to tune deeper optimisations across the project.
    - Default: `List.empty`.

- **`tpolecatDevModeOptions`**, **`tpolecatCiModeOptions`**, **`tpolecatReleaseModeOptions`**:
    - These keys provide customisation points for scalac options unique to each build mode. Use them to override or
      extend default configurations.

</details>

---

## Supplementary Plugins

<details>
<summary>ShuwariJsPlugin</summary>

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari-js" % "@VERSION@")
```

### Available Configuration Keys

- **`scalaJSLinkerConfig`**:
    - Configures the linker for Scala.js builds, automatically adjusting based on the current `buildMode`.

#### Linker Configurations Employed

- **Development Mode**:  
  Configured for faster builds and easier debugging. Uses **ES modules** for compatibility with modern JavaScript
  environments and applies the `FewestModules` strategy for minimal module splitting.

- **Release/Integration Mode**:  
  Optimised for production and integration builds. Uses the `SmallestModules` splitting strategy for reducing bundle
  sizes and enables **Closure Compiler** for advanced optimisations.

- **`tpolecatExcludeOptions`**:
    - Excludes Scala.js-incompatible scalac options, such as:
        - `explicitNulls`
        - JVM-specific checks like `checkMods`.

</details>

---

## Licence

<pre>
Copyright © Shuwari Africa Ltd. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "Licence"). You may obtain a copy at:
<a href="http://www.apache.org/licenses/LICENSE-2.0" target="_blank">http://www.apache.org/licenses/LICENSE-2.0</a>

Unless required by applicable law or agreed to in writing, software distributed under the Licence
is distributed on an"AS-IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND.
See the Licence for details.
</pre>