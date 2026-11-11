# sbt-shuwari

[![Build Status](https://github.com/shuwariafrica/sbt-shuwari/actions/workflows/build.yml/badge.svg)](https://github.com/shuwariafrica/sbt-shuwari/actions/workflows/build.yml)

Nothing to see here. Internal sbt plugin collection for Shuwari Africa projects.

## Modules

| Artifact             | Coordinates                                                  |
|----------------------|--------------------------------------------------------------|
| `sbt-shuwari`        | `"africa.shuwari.sbt" % "sbt-shuwari" % "0.16.0"`         |
| `sbt-shuwari-core`   | `"africa.shuwari.sbt" % "sbt-shuwari-core" % "0.16.0"`    |
| `sbt-shuwari-mode`   | `"africa.shuwari.sbt" % "sbt-shuwari-mode" % "0.16.0"`    |
| `sbt-shuwari-header` | `"africa.shuwari.sbt" % "sbt-shuwari-header" % "0.16.0"`  |
| `sbt-shuwari-scalac` | `"africa.shuwari.sbt" % "sbt-shuwari-scalac" % "0.16.0"`  |
| `sbt-shuwari-js`     | `"africa.shuwari.sbt" % "sbt-shuwari-js" % "0.16.0"`      |

`sbt-shuwari` bundles `sbt-shuwari-core`, `sbt-shuwari-header`, and `sbt-shuwari-scalac`. `sbt-shuwari-js` is
separate: it brings in sbt-scalajs and configures the Scala.js linker for each build mode.

## Licence

Copyright © Shuwari Africa Ltd. All rights reserved. Licensed under the Apache License, Version 2.0.
