package macroni.matcher

import macroni.compiler._
import macroni.helper.FailDescription

import org.specs2.matcher._

object CheckMatchers {
  //TODO: set messages
  def alwaysOk[T]: Matcher[T] = new AlwaysMatcher[T]
  def isEmpty[T]: Matcher[Seq[T]] = AnyMatchers.beEmpty
  def once[T]: Matcher[Seq[T]] = TraversableMatchers.haveSize(1)
  def nonEmpty[T]: Matcher[Seq[T]] = isEmpty.not
}

class MessageMatcher[M <: Message](matcher: Matcher[String]) extends Matcher[M] {
  override def apply[S <: M](expectable: Expectable[S]): MatchResult[S] = {
    val matchResult = matcher(createExpectable(expectable.value.msg))
    result(matchResult, expectable)
  }
}

object MessageMatcher {
  def warning(matcher: Matcher[String]) = new MessageMatcher[Warning](matcher)
  def warnings(matchers: Seq[Matcher[String]]) = SeqMessageMatcher.warning(matchers.map(warning))
  def error(matcher: Matcher[String]) = new MessageMatcher[Error](matcher)
  def errors(matchers: Seq[Matcher[String]]) = SeqMessageMatcher.error(matchers.map(error))
}

class SeqMessageMatcher[T <: Message](matchers: Seq[Matcher[T]], missingValue: T, describeMismatchMessage: String => String, describeMissingMessages: Seq[String] => String, describeTooManyMessages: Seq[T] => String) extends Matcher[Seq[T]] {
  import scala.compat.Platform.EOL

  private def excessCompilerMessages(messages: Seq[T]): Option[String] = {
    if (matchers.size > messages.size) {
      val excessMatchers = matchers.drop(messages.size)
      val results = excessMatchers.map(_(createExpectable(missingValue)))
      Some(describeMissingMessages(results.map(_.message)))
    } else if (matchers.size < messages.size) {
      val excessMessages = messages.drop(matchers.size)
      Some(describeTooManyMessages(excessMessages))
    } else None
  }

  // TODO exactly without order semantic?
  override def apply[S <: Seq[T]](expectable: Expectable[S]): MatchResult[S] = {
    val messages = expectable.value
    val matches = matchers.zip(messages).map { case (matcher, exp) =>
      matcher(createExpectable(exp)).updateMessage(describeMismatchMessage)
    }

    val excessMessages = excessCompilerMessages(messages).map(failure(_, expectable))
    result(MatchResult.sequence(matches ++ Seq(excessMessages).flatten), expectable)
  }
}

object SeqMessageMatcher {
  import scala.reflect.runtime.universe.NoPosition

  private val missingWarning = Warning(NoPosition, "")
  private val missingError = Error(NoPosition, "")

  def warning(matchers: Seq[Matcher[Warning]]) = new SeqMessageMatcher(matchers, missingWarning, FailDescription.mismatchCompilerWarning, FailDescription.missingCompilerWarnings, FailDescription.tooManyCompilerWarnings)
  def error(matchers: Seq[Matcher[Error]]) = new SeqMessageMatcher(matchers, missingError, FailDescription.mismatchCompilerError, FailDescription.missingCompilerErrors, FailDescription.tooManyCompilerErrors)
}
