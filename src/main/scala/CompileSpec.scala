package macroni

import macroni.compiler.{CompileResult, CompileFailure, CompileSuccess}
import macroni.matcher._
import macroni.macros.NamedMatcherMacro

import scala.reflect.runtime.universe.Tree
import org.specs2.mutable.Specification
import org.specs2.matcher.{AnyMatchers, Matcher}

trait CompileSpec extends Specification {
  implicit def TreeToWith(t: Tree): With = With(t)
  implicit def MatcherToCompileTreeMatcher(matcher: Matcher[CompileResult]): CompileTreeMatcher = new CompileTreeMatcher(matcher)
  implicit def StringToMatcher(msg: String): Matcher[String] = AnyMatchers.beEqualTo(msg)

  def compile = new SuccessCompileMatcher()
  def canWarn = compile.canWarn
  def warn = compile.withWarnings
  def abort = compile.withErrors

  def warn(matchers: Matcher[String]*): SuccessCompileMatcher = macro NamedMatcherMacro.compileWithWarnings
  def abort(matchers: Matcher[String]*): FailureCompileMatcher = macro NamedMatcherMacro.compileWithErrors
}
