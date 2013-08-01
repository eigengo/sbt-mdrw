package org.eigengo.sbtmdrw.renderers

import org.eigengo.sbtmdrw.MarkdownRenderer
import org.pegdown.ast.RootNode

class WordpressMarkdownRenderer private() extends MarkdownRenderer {

  def render(root: RootNode): String = {
    val visitor = new HtmlVisitor(_ => NoWrap) with WordpressHtmlVisitorFormat
    visitor.visit(root)
    visitor.toHtml
  }

}

object WordpressMarkdownRenderer {
  def apply(): WordpressMarkdownRenderer = new WordpressMarkdownRenderer()
}

private[renderers] trait WordpressHtmlVisitorFormat extends HtmlVisitorCodeFormat with HtmlVisitorHeadingFormat {
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