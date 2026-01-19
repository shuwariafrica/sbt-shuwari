package africa.shuwari.sbt

import scala.collection.immutable.ListSet

import sbt._

object ScalacOptionsPluginImport:

  final val ScalacOptions = africa.shuwari.sbt.ScalacOptions

  val compilerOptions = settingKey[ListSet[ScalacOption]]("Set specifying ScalacOptions to be used.")

  val basePackages = settingKey[Set[String]]("Base package name.")

  extension (opts: ListSet[ScalacOption])
    inline def scalac: List[String] = ScalacOptions.optionsList(opts)
    inline def exclude(excluded: ScalacOption*): ListSet[ScalacOption] = exclude(excluded)
    inline def exclude(excluded: IterableOnce[ScalacOption]): ListSet[ScalacOption] = opts.diff(excluded.iterator.toSet)
