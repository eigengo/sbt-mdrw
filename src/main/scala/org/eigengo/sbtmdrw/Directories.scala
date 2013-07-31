package org.eigengo.sbtmdrw

import java.io.File

case class Directories(sources: Seq[File], resources: Seq[File], outDir: Option[File]) {
  def addSrc(moreSources: Seq[File]): Directories = copy(sources = sources ++ moreSources)
  def addRes(moreResources: Seq[File]): Directories = copy(resources = resources ++ moreResources)
}