package org.eigengo.sbtmdrw.renderers

import org.eigengo.sbtmdrw.MarkdownRenderer
import org.pegdown.ast.RootNode
import sbt.{Logger, State}
import scala.util.{Success, Try}

class WordpressMarkdownRenderer private() extends MarkdownRenderer {

  def render[A](root: RootNode, log: Logger)(onComplete: Try[String] => A): A = {
    val visitor = new HtmlVisitor(_ => NoWrap) with WordpressHtmlVisitorFormat
    visitor.visit(root)
    onComplete(Success(visitor.toHtml))
  }

}

object WordpressMarkdownRenderer {
  def apply(): WordpressMarkdownRenderer = new WordpressMarkdownRenderer()
}

private[renderers] trait WordpressHtmlVisitorFormat extends HtmlVisitorCodeFormat with HtmlVisitorHeadingFormat {
  def codeBlockTags(kind: Option[String]): Tags =
    Tags("""[code language="%s"]""" format kind.getOrElse("scala"), "[/code]")

  def escapeCode(code: String): String = {
    code.
      replaceAll("&", "&amp;").
      replaceAll(">", "&gt;").
      replaceAll("<", "&lt;")
  }

  def headingTag(level: Int): Tags = Tags("<h%d>" format level, "</h%d>" format level)

}