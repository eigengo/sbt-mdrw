package org.eigengo.sbtmdrw

import sbt._
import sbt.complete.Parsers._
import java.io.{BufferedWriter, FileOutputStream, File, FileWriter}
import sbt.Load.BuildStructure
import java.rmi.activation.Activator
import org.eigengo.sbtmdrw.renderers.{WordpressMarkdownRenderer, ActivatorMarkdownRenderer}
import scala.io.Source

/**
 * Plugin for SBT that provides a task to convert Markdown files into other text-based formats, in the first instance,
 * simple HTML files suitable for the Typesafe Activator and for the Cake Solutions Blog.
 */
object MarkdownRewriterPlugin extends Plugin {

  override lazy val settings = Seq(
    Keys.commands += mdrwCommand
  )

  def renderers: PartialFunction[Option[String], MarkdownRenderer] = {
    case Some("activator") => ActivatorMarkdownRenderer()
    case Some("wordpress") => WordpressMarkdownRenderer()

    case _                 => ActivatorMarkdownRenderer() // TODO: some sensible default
  }

  private val args = (Space ~> StringBasic).*

  private lazy val mdrwCommand = Command("mdrw")(_ => args)(doCommand)

  def doCommand(state: State, args: Seq[String]): State = {
    val renderer = renderers(args.headOption)

    val extracted = Project.extract(state)
    val buildStruct = extracted.structure
    val buildUnit = buildStruct.units(buildStruct.root)
    val uri = buildStruct.root
    val projectList = {
      def getProjectList(proj: ResolvedProject): List[(ProjectRef, ResolvedProject)] = {
        def processAggregates(aggregates: List[ProjectRef]): List[(ProjectRef, ResolvedProject)] =
          aggregates match {
            case Nil =>
              List.empty
            case ref :: tail =>
              Project.getProject(ref, buildStruct).
                map(subProject => (ref -> subProject) +: getProjectList(subProject) ++: processAggregates(tail)).
                getOrElse(processAggregates(tail))
          }

        processAggregates(proj.aggregate.toList)
      }

      buildUnit.defined.flatMap {
        case (id, proj) => (ProjectRef(uri, id) -> proj) :: getProjectList(proj).toList
      }
    }

    projectList.foreach {
      case (_, project) => processProject(state, project, buildUnit.localBase, renderer)
    }

    state
  }

  private def isMarkdownFile(file: File): Boolean = {
    if (file.isDirectory) {
      false
    } else {
      val ext = file.ext
      ext == "md" || ext == "mdown" || ext == "markdown"
    }
  }

  private def processProject(state: State, project: ResolvedProject, projectRoot: File, renderer: MarkdownRenderer): Unit = {
    val tutorialBase = project.base / "tutorial"
    val tutorialTarget = projectRoot / "tutorial"
    state.log.info("Processing markdown files in " + tutorialBase + " to " + tutorialTarget)

    val markdownFiles = tutorialBase.listFiles(new FileFilter {
      def accept(pathname: File): Boolean = isMarkdownFile(pathname)
    })

    markdownFiles.foreach(processMarkdownFile(state, renderer, tutorialTarget))
  }

  private def processMarkdownFile(state: State, renderer: MarkdownRenderer, targetDirectory: File)(file: File): Unit = {
    state.log.info("Converting file " + file)

    new MarkdownRewriter(file, renderer).run() match {
      case Left(error)    => state.log.error(error)
      case Right(content) =>
        val w = new BufferedWriter(new FileWriter(targetDirectory / file.base + ".html"))
        w.write(content)
        w.close()
    }
  }

}
