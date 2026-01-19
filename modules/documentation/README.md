# sbt-shuwari

A collection of [sbt](https://scala-sbt.org) plugins providing unified build configuration for Scala projects.

[![Build Status](https://github.com/shuwariafrica/sbt-shuwari/actions/workflows/build.yml/badge.svg)](https://github.com/shuwariafrica/sbt-shuwari/actions/workflows/build.yml)

---

## Setup

Add to `project/plugins.sbt`:

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari" % "@VERSION@")
```

This includes all core plugins. Individual plugins may be added separately if preferred.

---

## Plugins

All plugins are `AutoPlugin` implementations that trigger automatically when their requirements are satisfied.

### ShuwariCorePlugin

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari-core" % "@VERSION@")
```

**Requires:** `JvmPlugin`

Provides project configuration utilities via extension methods on `Project`.

#### Extension Methods

| Method                                            | Description                                                                                               |
|---------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `shuwariProject`                                  | Applies organisation defaults (`organizationName`, `organizationHomepage`, `developers`, `versionScheme`) |
| `notPublished`                                    | Disables all publishing tasks                                                                             |
| `dependsOn(libraries: Def.Initialize[ModuleID]*)` | Adds library dependencies from settings                                                                   |

#### Settings Applied by `shuwariProject`

```scala
organizationName := "Shuwari Africa Ltd."
organizationHomepage := Some(url("https://shuwari.africa"))
developers := List(
  Developer(
    "shuwari-dev",
    "Shuwari Africa Ltd. Developer Team",
    "https://github.com/shuwariafrica",
    url("https://shuwari.africa/dev")
  )
)
versionScheme := Some("semver-spec")
```

#### Settings Applied by `notPublished`

```scala
publish / skip := true
publish := {}
publishLocal := {}
publishArtifact := false
```

#### Example

```scala
lazy val myProject = project
  .shuwariProject
  .notPublished
```

---

### BuildModePlugin

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari-mode" % "@VERSION@")
```

**Requires:** `JvmPlugin`

Provides build mode detection for environment-aware compilation and configuration.

#### Setting Keys

| Key         | Type      | Description                                                         |
|-------------|-----------|---------------------------------------------------------------------|
| `buildMode` | `Mode`    | Current build mode. Resolved from `BUILD_MODE` environment variable |
| `ci`        | `Boolean` | Whether the build is running in a CI environment                    |

#### Build Modes

The `Mode` sealed trait provides three values:

- `Mode.Development` — Local development (default)
- `Mode.Integration` — CI/integration builds
- `Mode.Release` — Production releases

#### Resolution Logic

1. Reads `BUILD_MODE` environment variable (case-insensitive: `development`, `integration`, `release`)
2. Defaults to `Mode.Development` if unset
3. Automatically promotes `Development` to `Integration` when CI is detected

#### CI Detection

The `ci` setting returns `true` when any of the following environment variables are present:

- `CI`
- `GITHUB_ACTIONS`
- `TF_BUILD` (Azure Pipelines)
- `BITBUCKET_BUILD_NUMBER`
- `TEAMCITY_VERSION`

#### Example

```scala
Compile / compile := {
  if (buildMode.value === Mode.Release) {
    // Release-specific logic
  }
  (Compile / compile).value
}
```

---

### ShuwariHeaderPlugin

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari-header" % "@VERSION@")
```

**Requires:** `ShuwariCorePlugin`, `HeaderPlugin` (sbt-header)

Automatically configures source file licence headers based on the project's `licenses` setting.

#### Setting Keys

| Key                     | Type             | Description                                           |
|-------------------------|------------------|-------------------------------------------------------|
| `headerCopyrightHolder` | `Option[String]` | Copyright holder name. Defaults to `organizationName` |

#### Licence Detection

The plugin inspects the `licenses` setting and applies the appropriate header:

| Detected Licence | Header Applied                         |
|------------------|----------------------------------------|
| Apache-2.0       | Apache License 2.0 header              |
| GPL-3.0 / GPLv3  | GNU GPL v3 header                      |
| MIT              | MIT licence header                     |
| Empty/None       | Internal software header (proprietary) |

#### Convenience Settings

```scala
apacheLicensed // Sets licenses := List(License.Apache2)
mitLicensed // Sets licenses := List(License.MIT)
gplLicensed // Sets licenses := List(License.GPL3_or_later)
internalSoftware // Sets licenses := List.empty
```

#### Extension Methods

| Method                | Description                           |
|-----------------------|---------------------------------------|
| `license(l: License)` | Sets the project licence              |
| `internalSoftware`    | Marks project as internal/proprietary |

#### Example

```scala
lazy val myProject = project
  .settings(apacheLicensed)

// Or using extension method
lazy val internal = project
  .internalSoftware
```

---

### ScalacOptionsPlugin

```scala
addSbtPlugin("africa.shuwari.sbt" % "sbt-shuwari-scalac" % "@VERSION@")
```

**Requires:** `BuildModePlugin`

Configures Scala 3 compiler options based on the active build mode. Built
on [typelevel/scalac-options](https://github.com/typelevel/scalac-options).

#### Setting Keys

| Key               | Type                    | Description                                    |
|-------------------|-------------------------|------------------------------------------------|
| `compilerOptions` | `ListSet[ScalacOption]` | Active compiler options                        |
| `basePackages`    | `Set[String]`           | Base package names for optimiser configuration |

#### Default Options

The following options are enabled by default (when supported by the Scala version):

- `-explain` — Detailed error explanations
- `-explain-types` — Detailed type mismatch explanations
- `-Yrequire-targetName` — Require `@targetName` annotations
- `-Yexplicit-nulls` — Distinguish `T` from `T | Null`
- `-Ycheck-reentrant` — Check for reentrant macro expansions
- `-language:strictEquality` — Require explicit `CanEqual` instances
- `-new-syntax` — Require new Scala 3 syntax
- `-Xmax-inlines:64` — Increased inline limit
- Fatal warning options from scalac-options

#### Mode-Specific Behaviour

| Mode                      | Behaviour                                                              |
|---------------------------|------------------------------------------------------------------------|
| `Development`             | Default options                                                        |
| `Integration` / `Release` | Default options + optimiser options (when `basePackages` is non-empty) |

#### Test Scope

The `Test` configuration automatically excludes:

- `-Yexplicit-nulls`
- `-language:strictEquality`

#### Extension Methods on `ListSet[ScalacOption]`

| Method                         | Description                                    |
|--------------------------------|------------------------------------------------|
| `scalac`                       | Converts to `List[String]` for `scalacOptions` |
| `exclude(opts: ScalacOption*)` | Removes specified options                      |

#### Additional Scala 3 Options

The plugin defines additional options beyond the base scalac-options library:

- `ScalacOptions.checkMods` — Check modifier consistency
- `ScalacOptions.checkMacros` — Check macro implementations
- `ScalacOptions.checkReentrant` — Check for reentrant macro expansions
- `ScalacOptions.explicitNulls` — Enable explicit nulls
- `ScalacOptions.requireTargetName` — Require `@targetName` for enums
- `ScalacOptions.experimentalCaptureChecking` — Capture checking (experimental)
- `ScalacOptions.experimentalErasedDefinitions` — Erased definitions (experimental)
- `ScalacOptions.experimentalInto` — `into` modifier (experimental)
- `ScalacOptions.experimentalPureFunctions` — Pure functions (experimental)
- `ScalacOptions.experimentalSaferExceptions` — Safer exceptions (experimental)

#### Example

```scala
// Add base packages for optimiser
basePackages := Set("com.example.myapp")

// Exclude specific options
Compile / compilerOptions := compilerOptions.value.exclude(
  ScalacOptions.explicitNulls
)
```

---

## Licence

Copyright © Shuwari Africa Ltd. All rights reserved. Licensed under the Apache License, Version 2.0.
