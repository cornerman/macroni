package macroni.helper

import macroni.compiler._

import scala.compat.Platform.EOL
import scala.reflect.runtime.universe.{showCode, Tree}

object FailDescription {
  import Colors._

  def highlightCode(tree: Tree) = highlight(showCode(tree))

  def describeSection(section: (String, String)) = {
    val (head, body) = section
    bold(red(s"--- $head: ---")) + EOL + body
  }

  def describeFailure(sections: (String, String)*) = {
    val descriptions = sections.map(describeSection)
    descriptions.mkString(EOL) + EOL + bold(red("---")) + EOL
  }

  def prependSource(source: Tree)(description: String) = {
    val sourceDesc = describeSection("source" -> highlightCode(source))
    sourceDesc + EOL + description
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
    describeFailure("source" -> highlightCode(source), "which does not contain" -> message)
  }

  def shouldHaveChild(source: Tree, message: String) = {
    describeFailure("source" -> highlightCode(source), "which does not have a direct child" -> message)
  }

  def mismatchCompilerWarning(message: String) = {
    describeFailure("compiles with unexpected warnings" -> message)
  }

  def mismatchCompilerError(message: String) = {
    describeFailure("has unexpected errors" -> message)
  }

  def missingCompilerWarnings(messages: Seq[String]) = {
    describeFailure("compiles with less warnings than expected, missing" -> messages.mkString(EOL))
  }

  def tooManyCompilerWarnings(messages: Seq[Warning]) = {
    describeFailure("compiles with more warnings than expected" -> messages.mkString(EOL))
  }

  def missingCompilerErrors(messages: Seq[String]) = {
    describeFailure("has less errors than expected, missing" -> messages.mkString(EOL))
  }

  def tooManyCompilerErrors(messages: Seq[Error]) = {
    describeFailure("has more errors than expected" -> messages.mkString(EOL))
  }
}
