package macroni

import macroni.compiler.CompileResult
import macroni.matcher._

import scala.reflect.runtime.universe.Tree
import org.specs2.mutable.Specification
import org.specs2.matcher.{AnyMatchers, Matcher}

trait CompileSpec extends Specification with TreeMatchers {
  implicit def MatcherToCompilingTreeMatcher(matcher: Matcher[CompileResult]): Matcher[Tree] = new CompilingTreeMatcher(matcher)
  implicit def MatcherToCompiledTreeMatcher(matcher: Matcher[Tree]): Matcher[CompileResult] = new CompiledTreeMatcher(matcher)
  implicit def StringToMatcher(msg: String): Matcher[String] = AnyMatchers.beEqualTo(msg)
  implicit def TreeToMatcher(tree: Tree): Matcher[Tree] = TreeMatchers.beEqualTo(tree)

  def compile = new SuccessCompileMatcher()
  def canWarn = compile.canWarn
  def warn = compile.withWarnings
  def warn(matchers: Matcher[String]*) = compile.withWarnings(matchers: _*)
  def abort = compile.withErrors
  def abort(matchers: Matcher[String]*) = compile.withErrors(matchers: _*)
}
