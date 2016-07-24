package macroni.matcher

import macroni.compiler._

import org.specs2.matcher._

class MessageMatcher[M <: Message](matcher: Matcher[String]) extends Matcher[M] {
  override def apply[S <: M](expectable: Expectable[S]): MatchResult[S] = {
    val matchResult = matcher(createExpectable(expectable.value.msg))
    result(matchResult, expectable)
  }
}

object MessageMatcher {
  def sequence[M](matchers: Seq[Matcher[M]]): Matcher[Seq[M]] = {
    val checks = matchers.map(m => ValueChecks.matcherIsValueCheck(m))
    TraversableMatchers.contain(TraversableMatchers.exactly(checks: _*)).inOrder
  }

  def info(matcher: Matcher[String]) = new MessageMatcher[Info](matcher)
  def warning(matcher: Matcher[String]) = new MessageMatcher[Warning](matcher)
  def error(matcher: Matcher[String]) = new MessageMatcher[Error](matcher)

  def infos(matchers: Seq[Matcher[String]]) = sequence(matchers.map(info))
  def warnings(matchers: Seq[Matcher[String]]) = sequence(matchers.map(warning))
  def errors(matchers: Seq[Matcher[String]]) = sequence(matchers.map(error))

  //TODO: set messages
  def isEmpty[T]: Matcher[Seq[T]] = AnyMatchers.beEmpty
  def once[T]: Matcher[Seq[T]] = TraversableMatchers.haveSize(1)
  def nonEmpty[T]: Matcher[Seq[T]] = isEmpty.not
}
