package org.eigengo.sbtmdrw.renderers

import org.pegdown.ast._
import scala.collection.mutable
import org.pegdown.ast.SimpleNode.Type

sealed trait Wrap
case class PrefixWith(html: String) extends Wrap
case object Skip extends Wrap
case object NoWrap extends Wrap

case class Tags(opening: String, closing: String)

case class Header(level: Int, html: String)

trait HtmlVisitorCodeFormat {

  def codeBlockTags(kind: Option[String]): Tags

  def escapeCode(code: String): String

}

trait HtmlVisitorHeadingFormat {

  def headingTag(level: Int): Tags

}

class HtmlVisitor(wrapper: Header => Wrap) extends Visitor {
  this: HtmlVisitorCodeFormat with HtmlVisitorHeadingFormat =>

  private[this] trait NoFormat extends HtmlVisitorCodeFormat with HtmlVisitorHeadingFormat {
    def codeBlockTags(kind: Option[String]): Tags = Tags("", "")

    def escapeCode(code: String): String = code

    def headingTag(level: Int): Tags = Tags("<h%d>" format level, "</h%d>" format level)
  }

  import scala.collection.JavaConversions._
  private val buffer: StringBuilder = new mutable.StringBuilder()

  def collectChildren(node: Node): String = {
    val childVisitor = new HtmlVisitor(wrapper) with NoFormat
    node.getChildren.foreach(_.accept(childVisitor))
    childVisitor.toHtml
  }

  private def appendFormat(text: String, arguments: Any*): Unit = {
    buffer.append(text.format(arguments:_*))
  }

  private def appendNL(): Unit = buffer.append('\n')

  def toHtml: String = buffer.toString()

  def visit(node: AbbreviationNode) {}

  def visit(node: AutoLinkNode) {}

  def visit(node: BlockQuoteNode) {}

  def visit(node: BulletListNode) {
    buffer.append("<ul>\n")
    visitChildren(node)
    buffer.append("</ul>\n")
  }

  def visit(node: CodeNode) {
    appendFormat("<code>%s</code>", node.getText)
  }

  def visit(node: DefinitionListNode) {}

  def visit(node: DefinitionNode) {}

  def visit(node: DefinitionTermNode) {}

  def visit(node: ExpImageNode) {
    appendFormat("""<img src="%s" title="%s"/>""", node.url, collectChildren(node))
  }

  def visit(node: ExpLinkNode) {
    appendFormat("""<a href="%s" target="_blank">%s</a>""", node.url, collectChildren(node))
  }

  def visit(node: HeaderNode) {
    val html = collectChildren(node)
    val header = Header(node.getLevel, html)
    val headerTags = headingTag(node.getLevel)

    wrapper(header) match {
      case PrefixWith(prefixHtml) =>
        buffer.append(prefixHtml)
        buffer.append(headerTags.opening)
        buffer.append(html)
        buffer.append(headerTags.closing)
        appendNL()
      case NoWrap =>
        buffer.append(headerTags.opening)
        buffer.append(html)
        buffer.append(headerTags.closing)
        appendNL()
      case Skip =>
        // do nothing
    }
  }

  def visit(node: HtmlBlockNode) {}

  def visit(node: InlineHtmlNode) {}

  def visit(node: ListItemNode) {
    buffer.append("<li>")
    visitChildren(node)
    buffer.append("</li>\n")
  }

  def visit(node: MailLinkNode) {}

  def visit(node: OrderedListNode) {}

  def visit(node: ParaNode) {
    appendFormat("<p>")
    visitChildren(node)
    appendFormat("</p>")
    appendNL()
  }

  def visit(node: QuotedNode) {}

  def visit(node: ReferenceNode) {}

  def visit(node: RefImageNode) {

  }

  def visit(node: RefLinkNode) {}

  def visit(node: RootNode) {
    visitChildren(node)
  }

  def visit(node: SimpleNode) {
    node.getType match {
      case Type.Linebreak => buffer.append(' ')
      case _              =>
    }
  }

  def visit(node: SpecialTextNode) {
  }

  def visit(node: StrongEmphSuperNode) {
    val tag = if (node.isStrong) "strong" else "em"
    buffer.append('<').append(tag).append('>')
    visitChildren(node)
    buffer.append("</").append(tag).append('>')
  }

  def visit(node: TableBodyNode) {}

  def visit(node: TableCaptionNode) {}

  def visit(node: TableCellNode) {}

  def visit(node: TableColumnNode) {}

  def visit(node: TableHeaderNode) {}

  def visit(node: TableNode) {}

  def visit(node: TableRowNode) {}

  def visit(node: VerbatimNode) {
    val tags = codeBlockTags(if (node.getType.isEmpty) None else Some(node.getType))
    buffer.append(tags.opening)
    buffer.append('\n')
    buffer.append(escapeCode(node.getText))
    buffer.append(tags.closing)
    buffer.append('\n')
  }

  def visit(node: WikiLinkNode) {}

  def visit(node: TextNode) {
    buffer.append(node.getText)
  }

  def visit(node: SuperNode) {
    visitChildren(node)
  }

  def visit(node: Node) {}

  // --

  protected def visitChildren(node: SuperNode) {
    node.getChildren.foreach(_.accept(this))
  }
}