package macroni.compiler

import scala.reflect.runtime.universe.{Tree, NoPosition}

sealed trait CompileResult {
  val source: Tree
  val notices: Seq[Notice]

  def infos = notices collect { case m: Info => m }
  def warnings = notices collect { case m: Warning => m }
}

object CompileResult {
  def reportedMessages(reporter: Reporter) = {
    val messages = reporter.infoList.map(Message(reporter)).toSeq
    val notices = messages collect { case e: Notice => e }
    val errors = messages collect { case e: Error => e }
    (notices, errors)
  }

  def apply(source: Tree, generated: Tree, reporter: Reporter): CompileResult = {
    val (notices, errors) = reportedMessages(reporter)
    if (errors.isEmpty) CompileSuccess(source, generated, notices)
    else CompileFailure(source, errors, notices)
  }

  def apply(source: Tree, e: Throwable, reporter: Reporter): CompileResult = {
    val (notices, errors) = reportedMessages(reporter)
    val thrownError = Error(NoPosition, e.getMessage)
    CompileFailure(source, errors :+ thrownError, notices)
  }
}

case class CompileSuccess(source: Tree, generated: Tree, notices: Seq[Notice]) extends CompileResult
case class CompileFailure(source: Tree, errors: Seq[Error], notices: Seq[Notice]) extends CompileResult
