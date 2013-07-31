package org.eigengo.sbtmdrw

import sbt._
import sbt.complete.Parsers._
import java.io.{File, FileWriter}
import sbt.Load.BuildStructure

/**
 * Plugin for SBT that provides a task to convert Markdown files into other text-based formats, in the first instance,
 * simple HTML files suitable for the Typesafe Activator and for the Cake Solutions Blog.
 */
object MarkdownRewriterPlugin extends Plugin {

  override lazy val settings = Seq(
    Keys.commands += mdrwCommand
  )

  private val args = (Space ~> StringBasic).*

  private lazy val mdrwCommand = Command("mdrw")(_ => args)(doCommand)

  def doCommand(state: State, args: Seq[String]): State = {
    val templateType = args.headOption.getOrElse("activator")

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
      case (_, project) => processProject(state, project, buildUnit.localBase, templateType)
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

  private def processProject(state: State, project: ResolvedProject, projectRoot: File, templateType: String): Unit = {
    val tutorialBase = project.base / "tutorial"
    val tutorialTarget = projectRoot / "tutorial"
    state.log.info("Processing markdown files in " + tutorialBase + " to " + tutorialTarget)

    val markdownFiles = tutorialBase.listFiles(new FileFilter {
      def accept(pathname: File): Boolean = isMarkdownFile(pathname)
    })
  }

}
