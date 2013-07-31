package org.eigengo.sbtmdrw.renderers

import org.pegdown.ast._
import scala.collection.mutable

sealed trait ShouldYield
case object ConsumeAndYield extends ShouldYield
case object ConsumeOnly     extends ShouldYield
case object YieldOnly       extends ShouldYield
case object SkipAndClear    extends ShouldYield

sealed trait BufferOperation
case object ClearBuffer extends BufferOperation
case object LeaveBuffer extends BufferOperation

class HtmlVisitor(shouldYield: (Node, => String) => ShouldYield, doYield: CharSequence => BufferOperation) extends Visitor {
  import scala.collection.JavaConversions._
  private val buffer: StringBuilder = new mutable.StringBuilder()

  private def yieldBuffer(): Unit = {
    doYield(buffer) match {
      case ClearBuffer => buffer.clear()
      case LeaveBuffer => // nothing
    }
  }

  def collectChildren(node: Node): String = {
    val childVisitor = new HtmlVisitor((_, _) => ConsumeOnly, _ => LeaveBuffer)
    node.getChildren.foreach(_.accept(childVisitor))
    childVisitor.toHtml
  }

  private def yieldVisit[U](node: Node)(f: => U) {
    shouldYield(node, collectChildren(node)) match {
      case ConsumeAndYield => f; yieldBuffer()
      case ConsumeOnly     => f
      case YieldOnly       => yieldBuffer()
      case SkipAndClear    => buffer.clear()
    }
  }

  private def appendFormat(text: String, arguments: Any*): Unit = {
    buffer.append(text.format(arguments:_*))
  }

  private def appendNL(): Unit = buffer.append('\n')

  def toHtml: String = buffer.toString()

  def visit(node: AbbreviationNode) {}

  def visit(node: AutoLinkNode) {}

  def visit(node: BlockQuoteNode) {}

  def visit(node: BulletListNode) {}

  def visit(node: CodeNode) {
    yieldVisit(node) {
      appendFormat("<code>%s</code>", node.getText)
    }
  }

  def visit(node: DefinitionListNode) {}

  def visit(node: DefinitionNode) {}

  def visit(node: DefinitionTermNode) {}

  def visit(node: ExpImageNode) {
    yieldVisit(node) {
      appendFormat("""<img src="%s" title="%s"/>""", node.url, collectChildren(node))
    }
  }

  def visit(node: ExpLinkNode) {
    yieldVisit(node) {
      appendFormat("""<a href="%s" target="_blank">%s</a>""", node.url, collectChildren(node))
    }
  }

  def visit(node: HeaderNode) {
    yieldVisit(node) {
      val level = if (node.getLevel > 1) node.getLevel else 2
      appendFormat("<h%d>", level)
      visitChildren(node)
      appendFormat("</h%d>", level)
      appendNL()
    }
  }

  def visit(node: HtmlBlockNode) {}

  def visit(node: InlineHtmlNode) {}

  def visit(node: ListItemNode) {}

  def visit(node: MailLinkNode) {}

  def visit(node: OrderedListNode) {}

  def visit(node: ParaNode) {
    yieldVisit(node) {
      appendFormat("<p>")
      visitChildren(node)
      appendFormat("</p>")
      appendNL()
    }
  }

  def visit(node: QuotedNode) {}

  def visit(node: ReferenceNode) {}

  def visit(node: RefImageNode) {
    println(node)
  }

  def visit(node: RefLinkNode) {}

  def visit(node: RootNode) {
    visitChildren(node)
    doYield(buffer)
  }

  def visit(node: SimpleNode) {
    yieldVisit(node) { }
  }

  def visit(node: SpecialTextNode) {}

  def visit(node: StrongEmphSuperNode) {}

  def visit(node: TableBodyNode) {}

  def visit(node: TableCaptionNode) {}

  def visit(node: TableCellNode) {}

  def visit(node: TableColumnNode) {}

  def visit(node: TableHeaderNode) {}

  def visit(node: TableNode) {}

  def visit(node: TableRowNode) {}

  def visit(node: VerbatimNode) {
    buffer.append("<code><pre>\n")
    buffer.append(node.getText)
    buffer.append("</pre></code>\n")
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