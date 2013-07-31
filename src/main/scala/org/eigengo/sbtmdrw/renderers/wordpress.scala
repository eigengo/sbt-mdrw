package org.eigengo.sbtmdrw.renderers

import org.eigengo.sbtmdrw.MarkdownRenderer
import org.pegdown.ast.RootNode
import scala.collection.mutable

class WordpressMarkdownRenderer extends MarkdownRenderer {
  private val visitor = new HtmlVisitor((_, _) => ConsumeOnly, _ => LeaveBuffer) with WordpressHtmlVisitorFormat

  def render(root: RootNode): String = {
    visitor.visit(root)
    visitor.toHtml
  }

}

object WordpressMarkdownRenderer {
  def apply(): WordpressMarkdownRenderer = new WordpressMarkdownRenderer()
}

trait WordpressHtmlVisitorFormat extends HtmlVisitorCodeFormat with HtmlVisitorHeadingFormat {
  def codeBlockTags(kind: Option[String]): Tags =
    Tags("""<pre class="brush:[%s]">""" format kind.getOrElse("scala"), "</pre>")

  def escapeCode(code: String): String = {
    code.
      replaceAll("&", "&amp;").
      replaceAll(">", "&gt;").
      replaceAll("<", "&lt;")
  }

  def headingTag(level: Int): Tags = Tags("<h%d>" format level, "</h%d>" format level)

}