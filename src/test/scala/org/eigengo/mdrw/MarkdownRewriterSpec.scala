package org.eigengo.mdrw

import org.specs2.mutable.Specification
import org.eigengo.sbtmdrw.{MarkdownRenderer, MarkdownRewriter}
import java.io.File
import org.eigengo.sbtmdrw.renderers.{WordpressMarkdownRenderer, ActivatorMarkdownRenderer}
import org.specs2.execute.Result
import scala.io.Source
import xsbti.AppConfiguration
import sbt.{Level, Logger}
import scala.util.{Success, Failure}

object MarkdownRewriterSpec {
  class CollectingLogger extends Logger {
    override def trace(t: => Throwable): Unit = ()
    override def log(level: Level.Value, message: => String): Unit = ()
    override def success(message: => String): Unit = ()
  }
}

class MarkdownRewriterSpec extends Specification {

  def verifyRewrite(renderer: => MarkdownRenderer, rendererSuffix: String)(base: String): Result = {
    def getFile(name: String): File = new File(getClass.getResource(name).toURI)

    val expected = Source.fromFile(getFile("/" + base + "-" + rendererSuffix)).mkString
    val log = new MarkdownRewriterSpec.CollectingLogger
    new MarkdownRewriter(getFile("/" + base + ".md"), renderer).run(log) {
      case Failure(t) => failure(t.getMessage)
      case Success(rendered) => rendered mustEqual expected
    }

  }

  "Activator format" >> {
    def activator = verifyRewrite(ActivatorMarkdownRenderer(), "activator.html") _

    "simple" in { activator("simple") }

    "two sections" in { activator("twosections") }

    "code blocks" in { activator("code") }

    "refs" in { activator("refs") }

    "lists" in { activator("lists") }

    "https://github.com/eigengo/sbt-mdrw/issues/8" in { activator("wsargent-8") }

  }

  "Wordpress format" >> {
    def wordpress = verifyRewrite(WordpressMarkdownRenderer(), "wordpress.html") _

    "simple" in { wordpress("simple") }

    "code blocks" in { wordpress("code") }

    "https://github.com/eigengo/sbt-mdrw/issues/8" in { wordpress("wsargent-8") }

  }

}
