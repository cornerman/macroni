package macroni.compiler

import scala.tools.reflect.FrontEnd
import scala.reflect.runtime.universe.{Tree, NoPosition}

sealed trait CompileResult {
  val source: Tree
  val messages: Seq[Message]

  def infos = messages collect { case m: Info => m }
  def warnings = messages collect { case m: Warning => m }
  def errors = messages collect { case m: Error => m }
}

object CompileResult {
  def apply(source: Tree, generated: Tree, reporter: FrontEnd): CompileResult = {
    val messages = reporter.infos.map(Message(reporter)).toSeq
    if (reporter.hasErrors)
      CompileFailure(source, messages)
    else
      CompileSuccess(source, generated, messages)
  }

  def apply(source: Tree, e: Throwable, reporter: FrontEnd): CompileResult = {
    CompileFailure(source, reporter.infos.map(Message(reporter)).toSeq :+ Error(NoPosition, e.getMessage))
  }
}

case class CompileSuccess(source: Tree, generated: Tree, messages: Seq[Message]) extends CompileResult {
  assert(errors.isEmpty)
}

case class CompileFailure(source: Tree, messages: Seq[Message]) extends CompileResult {
  assert(errors.nonEmpty)
}
