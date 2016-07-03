package macroni.matcher

import macroni.compiler._
import macroni.compare.ExpectedCode
import macroni.macros.NamedMatcherMacro
import macroni.helpers.ErrorDescription._

import org.specs2.matcher._
import scala.reflect.runtime.universe.Tree

object CheckMatchers {
  //TODO: set messages
  def alwaysOk[T]: Matcher[T] = new ValueCheckMatcher(ValueCheck.alwaysOk)
  def isEmpty[T]: Matcher[Seq[T]] = AnyMatchers.beEmpty
  def once[T]: Matcher[Seq[T]] = TraversableMatchers.haveSize(1)
  def nonEmpty[T]: Matcher[Seq[T]] = isEmpty.not
}

class MessageMatcher[M <: Message](matcher: Matcher[String]) extends Matcher[M] {
  override def apply[S <: M](expectable: Expectable[S]): MatchResult[S] = {
    val matchResult = matcher(createExpectable(expectable.value.msg))
    result(matchResult, expectable)
  }

  override def toString = matcher.toString
}

class CompileTreeMatcher(matcher: Matcher[CompileResult]) extends Matcher[Tree] {
  override def apply[S <: Tree](expectable: Expectable[S]): MatchResult[S] = {
    val compiled = Compiler(expectable.value)
    val matchResult = matcher(createExpectable(compiled))
    result(matchResult, expectable)
  }
}

trait CompileMatcher[M <: CompileMatcher[M]] extends Matcher[CompileResult] {
  val hasValidWarnings: Matcher[Seq[Warning]]
  def copy(hasValidWarnings: Matcher[Seq[Warning]]): M

  def canWarn = copy(CheckMatchers.alwaysOk)
  def withWarning = copy(CheckMatchers.once)
  def withWarnings = copy(CheckMatchers.nonEmpty)
  def matchWarning(matcher: Matcher[String]) = matchWarnings(matcher)
  def matchWarnings(matchers: Matcher[String]*) = copy(new SeqMatcherOfMatchers(matchers.map(m => new MessageMatcher[Warning](m))))

  def withWarning(matcher: Matcher[String]): SuccessCompileMatcher = macro NamedMatcherMacro.compileWithWarning
  def withWarnings(matchers: Matcher[String]*): SuccessCompileMatcher = macro NamedMatcherMacro.compileWithWarnings

  def withError = new FailureCompileMatcher(CheckMatchers.once, hasValidWarnings)
  def withErrors = new FailureCompileMatcher(CheckMatchers.nonEmpty, hasValidWarnings)
  def matchError(matcher: Matcher[String]) = matchErrors(matcher)
  def matchErrors(matchers: Matcher[String]*) = new FailureCompileMatcher(new SeqMatcherOfMatchers(matchers.map(m => new MessageMatcher[Error](m))), hasValidWarnings)

  def withError(matcher: Matcher[String]): FailureCompileMatcher = macro NamedMatcherMacro.compileWithError
  def withErrors(matchers: Matcher[String]*): FailureCompileMatcher = macro NamedMatcherMacro.compileWithErrors
}

class SuccessCompileMatcher(val hasValidWarnings: Matcher[Seq[Warning]] = CheckMatchers.isEmpty) extends CompileMatcher[SuccessCompileMatcher] {

  def copy(hasValidWarnings: Matcher[Seq[Warning]]) = new SuccessCompileMatcher(hasValidWarnings)

  def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    expectable.value match {
      case c@CompileSuccess(_, gen, _) =>
        val matchedWarnings = hasValidWarnings(createExpectable(c.warnings))
        result(matchedWarnings, expectable)
      case CompileFailure(source, errors, notices) =>
        MatchFailure("", compileError(source, notices ++ errors), expectable)
    }
  }

  def to(tree: Tree) = this and (new EqualsMatcher(tree))
  def containing(snippets: ExpectedCode*) = this and (new ContainsMatcher(snippets))
}

class FailureCompileMatcher(val hasValidErrors: Matcher[Seq[Error]] = CheckMatchers.nonEmpty, val hasValidWarnings: Matcher[Seq[Warning]]) extends CompileMatcher[FailureCompileMatcher] {

  def copy(hasValidWarnings: Matcher[Seq[Warning]]) = new FailureCompileMatcher(hasValidErrors, hasValidWarnings)

  def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    expectable.value match {
      case CompileSuccess(_, _, _) =>
        MatchFailure("", "Code compiles", expectable)
      case c@CompileFailure(source, errors, notices) =>
        val matchedWarnings = hasValidWarnings(createExpectable(c.warnings))
        val matchedErrors = hasValidErrors(createExpectable(errors))
        result(MatchResult.sequence(Seq(matchedWarnings, matchedErrors)), expectable)
    }
  }
}
