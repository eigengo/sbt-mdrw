package org.eigengo.sbtmdrw

import org.pegdown.ast.RootNode

trait MarkdownRenderer {

  def render(root: RootNode): String

}
