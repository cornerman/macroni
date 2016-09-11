package macroni

import scala.reflect.runtime.universe.Tree
import org.specs2.matcher.{BeEqualTo, Matcher}

import macroni.compiler.CompileResult
import macroni.matcher.{CompilingTreeMatcher, CompileMatchers}

trait CompileSpec extends TreeSpec with CompileMatchers {
  implicit def MatcherToCompilingTreeMatcher(matcher: Matcher[CompileResult]): Matcher[Tree] = new CompilingTreeMatcher(matcher)
  implicit def StringToMatcher(msg: String): Matcher[String] = new BeEqualTo(msg)
}
