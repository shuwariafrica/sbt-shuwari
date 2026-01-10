addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.1.1")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.8.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.4")

excludeDependencies += "org.scala-lang.modules" % "scala-collection-compat_2.13" // TODO: Remove once sbt-scalafmt is updated to use scala-collection-compat built against Scala 3
