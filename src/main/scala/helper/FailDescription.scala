package macroni.helper

import macroni.compiler._

import scala.compat.Platform.EOL
import scala.reflect.runtime.universe.{showCode, Tree}

object FailDescription {
  import Colors._

  private def highlightCode(tree: Tree) = highlight(showCode(tree))

  private def describeSection(section: (String, String)) = {
    val (head, body) = section
    bold(red(s"--- $head: ---")) + EOL + body
  }

  private def describeFailure(sections: (String, String)*) = {
    val descriptions = sections.map(describeSection)
    descriptions.mkString(EOL) + EOL + bold(red("---")) + EOL
  }

  def shouldCompile(result: CompileFailure) = {
    import result._
    describeFailure("source" -> highlightCode(source), "which doesn't compile but should" -> (notices ++ errors).mkString(EOL))
  }

  def shouldNotCompile(result: CompileSuccess) = {
    import result._
    describeFailure("source" -> highlightCode(source), "which compiles but shouldn't" -> notices.mkString(EOL))
  }

  def shouldContain(source: Tree, message: String) = {
    describeFailure("source" -> highlightCode(source), "which does not contain" -> highlight(message))
  }

  def shouldHaveChild(source: Tree, message: String) = {
    describeFailure("source" -> highlightCode(source), "which does not have a direct child" -> highlight(message))
  }

  def compilerWarnings(source: Tree, description: String) = {
    describeFailure("source" -> highlightCode(source), "which has unexpected warnings" -> description)
  }

  def compilerErrors(source: Tree, description: String) = {
    describeFailure("source" -> highlightCode(source), "which has unexpected errors" -> description)
  }
}
