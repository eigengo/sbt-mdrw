package org.eigengo.mdrw

import org.specs2.mutable.Specification
import org.eigengo.sbtmdrw.{MarkdownRenderer, MarkdownRewriter}
import java.io.File
import org.eigengo.sbtmdrw.renderers.ActivatorMarkdownRenderer
import org.specs2.execute.Result
import scala.io.Source

class MarkdownRewriterSpec extends Specification {

  def verifyRewrite(renderer: => MarkdownRenderer, rendererSuffix: String)(base: String): Result = {
    def getFile(name: String): File = new File(getClass.getResource(name).toURI)

    val expected = Source.fromFile(getFile("/" + base + "-" + rendererSuffix)).mkString
    val rendered = new MarkdownRewriter(getFile("/" + base + ".md"), renderer).run().right.get
    rendered mustEqual expected
  }

  "Activator format" >> {
    def activator = verifyRewrite(ActivatorMarkdownRenderer(), "activator.html") _

    /*
    "all" in {
      new MarkdownRewriter(getMarkdownFile("/all.md"), ActivatorMarkdownRenderer()).run()
    }

    "codepara" in {
      new MarkdownRewriter(getMarkdownFile("/code.md"), ActivatorMarkdownRenderer()).run()
    }

    "hr" in {
      new MarkdownRewriter(getMarkdownFile("/hr.md"), ActivatorMarkdownRenderer()).run()
    }
    */

    "simple" in { activator("simple") }

    "two sections" in { activator("twosections") }

    "code blocks" in { activator("code") }

    "refs" in { activator("refs") }

  }

}
