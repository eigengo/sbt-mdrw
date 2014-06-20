package org.eigengo.sbtmdrw

import org.pegdown.ast.RootNode
import sbt.Logger
import scala.util.Try

trait MarkdownRenderer {

  def render[A](root: RootNode, log: Logger)(onComplete: Try[String] => A): A

}
