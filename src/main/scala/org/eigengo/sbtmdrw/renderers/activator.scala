package org.eigengo.sbtmdrw.renderers

import org.pegdown.ast._
import scala.collection.mutable
import org.eigengo.sbtmdrw.MarkdownRenderer

class ActivatorMarkdownRenderer extends MarkdownRenderer {
  private var openDiv = false

  private val prefix =
    """<html>
      |<head>
      |    <title>%s</title>
      |</head>
      |<body>
      |""".stripMargin

  private val suffix =
    """</body>
      |</html>
      |""".stripMargin

  private val buffer: mutable.StringBuilder = new mutable.StringBuilder()

  private def wrap(header: Header): Wrap = {
    if (header.level == 1 && buffer.isEmpty) {
      buffer.append(prefix format header.html)
      Skip
    } else {
      val html = if (openDiv) "</div>\n<div>\n" else "<div>\n"
      openDiv = true

      PrefixWith(html)
    }
  }

  private val visitor = new HtmlVisitor(wrap) with ActivatorHtmlVisitorFormat

  def render(root: RootNode): String = {
    visitor.visit(root)
    buffer.append(visitor.toHtml)
    if (openDiv) buffer.append("</div>\n")
    buffer.append(suffix)
    buffer.toString()
  }

}

object ActivatorMarkdownRenderer {
  def apply(): ActivatorMarkdownRenderer = new ActivatorMarkdownRenderer()
}

trait ActivatorHtmlVisitorFormat extends HtmlVisitorCodeFormat with HtmlVisitorHeadingFormat {
  def codeBlockTags(kind: Option[String]): Tags = Tags("<code><pre>", "</pre></code>")

  def escapeCode(code: String): String = code

  def headingTag(level: Int): Tags = {
    val l = if (level == 1) 2 else level
    Tags("<h%d>" format l, "</h%d>" format l)
  }
}