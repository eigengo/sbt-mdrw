package org.eigengo.sbtmdrw

import sbt._
import sbt.complete.Parsers._
import java.io.{BufferedWriter, FileOutputStream, File, FileWriter}
import sbt.Load.BuildStructure
import java.rmi.activation.Activator
import org.eigengo.sbtmdrw.renderers.{WordpressMarkdownRenderer, ActivatorMarkdownRenderer}
import scala.io.Source
import scala.util.{Success, Failure}

/**
 * Plugin for SBT that provides a task to convert Markdown files into other text-based formats, in the first instance,
 * simple HTML files suitable for the Typesafe Activator and for the Cake Solutions Blog.
 */
object MarkdownRewriterPlugin extends Plugin {

  val renderers = SettingKey[Map[String, MarkdownRenderer]]("renderers") //Seq(ActivatorMarkdownRenderer(), WordpressMarkdownRenderer()))

  override lazy val settings = Seq(
    Keys.commands += mdrwCommand,
    renderers <<= renderers ?? defaultRenderes
  )

  private val defaultRenderer = ActivatorMarkdownRenderer()
  private val defaultRenderes = Map("activator" -> defaultRenderer, "wordpress" -> WordpressMarkdownRenderer())

  private val args = (Space ~> StringBasic).*

  private lazy val mdrwCommand = Command("mdrw")(_ => args)(doCommand)

  def doCommand(state: State, args: Seq[String]): State = {
    val extracted = Project.extract(state)
    val buildStruct = extracted.structure
    val availableRenderers: Option[Map[String, MarkdownRenderer]] = renderers in extracted.currentRef get buildStruct.data

    // I could have used some ``for`` comprehension or a ``map`` on the ``rendererNameOption``, but this
    // is a bit more readable. Also note that I use ``rendererNameOption`` rather than using ``args.headOption``
    // directly to allow me to change the ``args`` in the future
    val rendererNameOption = args.headOption
    val renderer: MarkdownRenderer = rendererNameOption match {
      case Some(rendererName) => availableRenderers.flatMap(_.get(rendererName)).getOrElse(defaultRenderer)
      case None               => defaultRenderer
    }

    // Collect *all* projects in this project's structure. I want to have a list of all sub-modules so that I can
    // work on each sub-module's markdown files
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

    // finally, process each project's markdown files, rendering to ``buildUnit.localBase``
    projectList.foreach {
      case (_, project) => processProject(state, project, buildUnit.localBase, renderer)
    }

    state
  }


  private def processProject(state: State, project: ResolvedProject, projectRoot: File, renderer: MarkdownRenderer): State = {
    // this is a bit more readable and a bit more Scala-esque variant of the same lines
    // in the implementation of the ``FileFilter``
    def isMarkdownFile(file: File): Boolean = {
      if (file.isDirectory) {
        false
      } else {
        val ext = file.ext
        ext == "md" || ext == "mdown" || ext == "markdown"
      }
    }

    val tutorialBase = project.base / "tutorial"    // we are searching for files in $project/tutorial
    val tutorialTarget = projectRoot / "tutorial"   // we are writing the files to $root/tutorial
    state.log.info("Processing markdown files in " + tutorialBase + " to " + tutorialTarget)

    // find the Markdown files
    val markdownFiles = tutorialBase.listFiles(new FileFilter {
      def accept(pathname: File): Boolean = isMarkdownFile(pathname)
    })

    // render each into the requested output
    markdownFiles.foreach(processMarkdownFile(state, renderer, tutorialTarget))

    state
  }

  private def processMarkdownFile(state: State, renderer: MarkdownRenderer, targetDirectory: File)(file: File): State = {
    state.log.info("Converting file " + file)

    new MarkdownRewriter(file, renderer).run(state.log) {
      case Failure(error)   =>
        state.log.error(error.getMessage)
        state.fail
      case Success(content) =>
        val w = new BufferedWriter(new FileWriter(targetDirectory / file.base + ".html"))
        w.write(content)
        w.close()
        state
    }
  }

}
