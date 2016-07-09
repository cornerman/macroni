package macroni.matcher

import macroni.compiler._
import macroni.helper.FailDescription

import org.specs2.matcher._
import scala.reflect.runtime.universe.Tree

class CompilingTreeMatcher(matcher: Matcher[CompileResult]) extends Matcher[Tree] {
  override def apply[S <: Tree](expectable: Expectable[S]): MatchResult[S] = {
    val compiled = Compiler(expectable.value)
    val matchResult = matcher(createExpectable(compiled))
    result(matchResult, expectable)
  }
}

trait CompileMatcher[M <: CompileMatcher[M]] extends Matcher[CompileResult] {
  val warningMatcher: Matcher[Seq[Warning]]
  def matchWarnings(warningMatcher: Matcher[Seq[Warning]]): M
  def matchErrors(errorMatcher: Matcher[Seq[Error]]) = new FailureCompileMatcher(errorMatcher, warningMatcher)

  def canWarn = matchWarnings(CheckMatchers.alwaysOk)
  def withWarning = matchWarnings(CheckMatchers.once)
  def withWarnings = matchWarnings(CheckMatchers.nonEmpty)
  def withWarning(matcher: Matcher[String]) = withWarnings(matcher)
  def withWarnings(matchers: Matcher[String]*) = matchWarnings(MessageMatcher.warnings(matchers))

  def withError = matchErrors(CheckMatchers.once)
  def withErrors = matchErrors(CheckMatchers.nonEmpty)
  def withError(matcher: Matcher[String]) = withErrors(matcher)
  def withErrors(matchers: Matcher[String]*) = matchErrors(MessageMatcher.errors(matchers))
}

class SuccessCompileMatcher(val warningMatcher: Matcher[Seq[Warning]] = CheckMatchers.isEmpty) extends CompileMatcher[SuccessCompileMatcher] {

  def matchWarnings(warningMatcher: Matcher[Seq[Warning]]) = new SuccessCompileMatcher(warningMatcher)

  def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    expectable.value match {
      case c: CompileSuccess =>
        val matchedWarnings = warningMatcher(createExpectable(c.warnings))
        val res = result(matchedWarnings, expectable)
        res.updateMessage(FailDescription.prependSource(c.source))
      case c: CompileFailure => failure(FailDescription.shouldCompile(c), expectable)
    }
  }

  def to(matchers: Matcher[Tree]*) = this and new CompiledTreeMatcher(matchers.fold(new AlwaysMatcher)((a,b) => a and b))
}

class FailureCompileMatcher(val errorMatcher: Matcher[Seq[Error]] = CheckMatchers.nonEmpty, val warningMatcher: Matcher[Seq[Warning]] = CheckMatchers.nonEmpty) extends CompileMatcher[FailureCompileMatcher] {

  def matchWarnings(warningMatcher: Matcher[Seq[Warning]]) = new FailureCompileMatcher(errorMatcher, warningMatcher)

  def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    expectable.value match {
      case c: CompileSuccess => failure(FailDescription.shouldNotCompile(c), expectable)
      case c: CompileFailure =>
        val matchedWarnings = warningMatcher(createExpectable(c.warnings))
        val matchedErrors = errorMatcher(createExpectable(c.errors))
        val res = result(MatchResult.sequence(Seq(matchedWarnings, matchedErrors)), expectable)
        res.updateMessage(FailDescription.prependSource(c.source))
    }
  }
}
