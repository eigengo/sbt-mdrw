package org.eigengo.sbtmdrw

import java.io.File
import org.pegdown.{LinkRenderer, ToHtmlSerializer, Extensions, PegDownProcessor}
import scala.io.Source

class MarkdownRewriter(source: File, renderer: MarkdownRenderer) {
  val sourceCharacters = Source.fromFile(source).iter.toArray

  def run(): Either[String, String] = {
    val processor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS | Extensions.HARDWRAPS | Extensions.TABLES | Extensions.DEFINITIONS)
    val root = processor.parseMarkdown(sourceCharacters)

    val rendered = renderer.render(root)

    Right(rendered)
  }

}
