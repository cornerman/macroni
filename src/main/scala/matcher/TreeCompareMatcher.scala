package macroni.matcher

import macroni.compiler.{CompileResult, CompileSuccess, CompileFailure}
import macroni.helpers.MatchDescription._

import org.specs2.matcher._
import scala.compat.Platform.EOL
import scala.reflect.runtime.universe.{showCode, Tree}

trait ExpectedCode { val code: Tree }
case class With(code: Tree) extends ExpectedCode
case class Not(code: Tree) extends ExpectedCode

class EqualsMatcher(expected: Tree) extends Matcher[CompileResult] {
  override def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    expectable.value match {
      case CompileSuccess(source, gen, _) =>
        val matchResult = AnyMatchers.beEqualTo(showCode(expected))(createExpectable(showCode(gen)))
        result(matchResult, expectable)
      case c: CompileFailure => MatchFailure("", compileError(c), expectable)
    }
  }
}

class ContainsMatcher(snippets: Seq[ExpectedCode]) extends Matcher[CompileResult] {
  override def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    expectable.value match {
      case CompileSuccess(source, gen, _) =>
        val matchResults = snippets.map {
          case With(code) => StringMatchers.contain(showCode(code))(createExpectable(showCode(gen)))
          case Not(code) => StringMatchers.contain(showCode(code)).not(createExpectable(showCode(gen)))
        }
        result(MatchResult.sequence(matchResults), expectable)
      case c: CompileFailure => MatchFailure("", compileError(c), expectable)
    }
  }
}
