package macroni.helpers

import macroni.compiler.Message
import macroni.compare.{ExpectedCode, Not, With}

import scala.compat.Platform.EOL
import scala.reflect.runtime.universe.{showCode, Tree}

object ErrorDescription {
  import Colors._

  private def code(tree: Tree) = highlight(showCode(tree))

  private def error(sections: (String, String)*) = {
    sections.map { case (head, body) =>
      bold(red(s"--- $head: ---")) + EOL + body
    }.mkString(EOL) + EOL + bold(red("----------")) + EOL
  }

  def compileError(source: Tree, messages: Seq[Message]) = {
    error("source" -> code(source), "which doesn't compile" -> messages.mkString(EOL))
  }

  def compareError(source: Tree, generated: Tree, expected: ExpectedCode) = {
    val failMsg = expected match {
      case With(_) => "which doesn't contain"
      case Not(_) => "which contains but shouldn't"
    }

    error("source" -> code(source), "generated" -> code(generated), failMsg -> code(expected.code))
  }

  def compareError(source: Tree, generated: Tree, expected: Tree) = {
    error("source" -> code(source), "generated" -> code(generated), "which is not equal to" -> code(expected))
  }
}

