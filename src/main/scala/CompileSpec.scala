package macroni

import macroni.compiler.CompileResult
import macroni.matcher._

import scala.reflect.runtime.universe.Tree
import org.specs2.mutable.Specification
import org.specs2.matcher.Matcher

trait TreeSpec extends Specification with TreeMatchers {
  implicit def TreeToMatcher(tree: Tree): Matcher[Tree] = beEqualTo(tree)
}

trait CompileSpec extends TreeSpec {
  implicit def MatcherToCompilingTreeMatcher(matcher: Matcher[CompileResult]): Matcher[Tree] = new CompilingTreeMatcher(matcher)
  implicit def StringToMatcher(msg: String): Matcher[String] = beEqualTo(msg)

  def compile = new SuccessCompileMatcher()
  def canWarn = compile.canWarn
  def warn = compile.withWarnings
  def warn(matchers: Matcher[String]*) = compile.withWarnings(matchers: _*)
  def abort = compile.withErrors
  def abort(matchers: Matcher[String]*) = compile.withErrors(matchers: _*)
}
