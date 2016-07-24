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

  def canWarn = matchWarnings(new AlwaysMatcher)
  def withWarning = matchWarnings(MessageMatcher.once)
  def withWarnings = matchWarnings(MessageMatcher.nonEmpty)
  def withWarning(matcher: Matcher[String]) = withWarnings(matcher)
  def withWarnings(matchers: Matcher[String]*) = matchWarnings(MessageMatcher.warnings(matchers))

  def withError = matchErrors(MessageMatcher.once)
  def withErrors = matchErrors(MessageMatcher.nonEmpty)
  def withError(matcher: Matcher[String]) = withErrors(matcher)
  def withErrors(matchers: Matcher[String]*) = matchErrors(MessageMatcher.errors(matchers))
}

class SuccessCompileMatcher(val warningMatcher: Matcher[Seq[Warning]] = MessageMatcher.isEmpty) extends CompileMatcher[SuccessCompileMatcher] {
  def matchWarnings(warningMatcher: Matcher[Seq[Warning]]) = new SuccessCompileMatcher(warningMatcher)

  def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    import FailDescription._
    expectable.value match {
      case c: CompileSuccess =>
        val warns = warningMatcher(createExpectable(c.warnings))
        result(warns.updateMessage(compilerWarnings(c.source, _)), expectable)
      case c: CompileFailure => failure(shouldCompile(c), expectable)
    }
  }

  def to(matchers: Matcher[Tree]*) = this and new CompiledTreeMatcher(matchers.fold(new AlwaysMatcher)((a,b) => a and b))
}

class FailureCompileMatcher(val errorMatcher: Matcher[Seq[Error]] = MessageMatcher.nonEmpty, val warningMatcher: Matcher[Seq[Warning]] = MessageMatcher.isEmpty) extends CompileMatcher[FailureCompileMatcher] {
  def matchWarnings(warningMatcher: Matcher[Seq[Warning]]) = new FailureCompileMatcher(errorMatcher, warningMatcher)

  def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    import FailDescription._
    expectable.value match {
      case c: CompileSuccess => failure(shouldNotCompile(c), expectable)
      case c: CompileFailure =>
        val warns = warningMatcher(createExpectable(c.warnings))
        val aborts = errorMatcher(createExpectable(c.errors))
        val matches = Seq(warns.updateMessage(compilerWarnings(c.source, _)), aborts.updateMessage(compilerErrors(c.source, _)))
        result(MatchResult.sequence(matches), expectable)
    }
  }
}
