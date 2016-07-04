package macroni.helpers

import macroni.compiler.{CompileFailure, CompileSuccess, Message}
import macroni.matcher.{ExpectedCode, Not, With}

import scala.compat.Platform.EOL
import scala.reflect.runtime.universe.{showCode, Tree}

object MatchDescription {
  import Colors._

  def highlightCode(tree: Tree) = highlight(showCode(tree))

  def describeError(sections: (String, String)*) = {
    sections.map { case (head, body) =>
      bold(red(s"--- $head: ---")) + EOL + body
    }.mkString(EOL) + EOL + bold(red("----------")) + EOL
  }

  def compileError(result: CompileSuccess) = {
    import result._
    describeError("source" -> highlightCode(source), "which compiles but shouldn't" -> notices.mkString(EOL))
  }

  def compileError(result: CompileFailure) = {
    import result._
    describeError("source" -> highlightCode(source), "which doesn't compile" -> (notices ++ errors).mkString(EOL))
  }
}
