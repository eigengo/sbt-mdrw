package org.eigengo.sbtmdrw.renderers

import org.pegdown.ast._
import scala.collection.mutable
import org.eigengo.sbtmdrw.MarkdownRenderer
import org.pegdown.ast.SimpleNode.Type

class ActivatorMarkdownRenderer extends MarkdownRenderer {

  private val prefix =
    """<html>
      |<head>
      |    <title>%s</title>
      |</head>
      |<body>
      |""".stripMargin

  private val suffix =
    """
      |</body>
      |</html>
      |""".stripMargin

  private val buffer: mutable.StringBuilder = new mutable.StringBuilder()

  private def shouldYield(node: Node, children: => String): ShouldYield = node match {
    case s: SimpleNode if s.getType == Type.HRule           => YieldOnly
    case h: HeaderNode if h.getLevel == 1 && buffer.isEmpty => buffer.append(prefix format children); SkipAndClear
    case _                                                  => ConsumeOnly
  }

  private def wrap(soFar: CharSequence): BufferOperation = {
    buffer.append("<div>\n")
    buffer.append(soFar)
    buffer.append("\n</div>\n")

    ClearBuffer
  }

  private val visitor = new HtmlVisitor(shouldYield, wrap) with ActivatorHtmlVisitorFormat

  def render(root: RootNode): String = {
    visitor.visit(root)
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