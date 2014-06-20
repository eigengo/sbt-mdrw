package org.eigengo.sbtmdrw

import java.io.File
import org.pegdown.{LinkRenderer, ToHtmlSerializer, Extensions, PegDownProcessor}
import scala.io.Source
import sbt.{Logger, State}
import scala.util.Try

class MarkdownRewriter(source: File, renderer: MarkdownRenderer) {
  val sourceCharacters = Source.fromFile(source).iter.toArray

  def run[A](log: Logger)(onComplete: Try[String] => A): A = {
    val processor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS | Extensions.HARDWRAPS | Extensions.TABLES | Extensions.DEFINITIONS)
    val root = processor.parseMarkdown(sourceCharacters)

    renderer.render(root, log)(onComplete)
  }

}
