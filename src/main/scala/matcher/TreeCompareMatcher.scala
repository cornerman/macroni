package macroni.matcher

import macroni.compiler.{CompileResult, CompileSuccess, CompileFailure}
import macroni.compare.ExpectedCode
import macroni.compare.TreeComparison._
import macroni.helpers.ErrorDescription._

import org.specs2.matcher._
import scala.compat.Platform.EOL
import scala.reflect.runtime.universe.Tree

class EqualsMatcher(expected: Tree) extends Matcher[CompileResult] {
  override def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    expectable.value match {
      case CompileSuccess(source, generated, _) =>
        result(equalsSnippet(generated, expected), "Are equal", compareError(source, generated, expected), expectable)
      case CompileFailure(source, msgs) => MatchFailure("", compileError(source, msgs), expectable)
    }
  }
}

class ContainsMatcher(snippets: Seq[ExpectedCode]) extends Matcher[CompileResult] {
  override def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    expectable.value match {
      case CompileSuccess(source, generated, _) =>
        val failures = unexpectedSnippets(source, generated, snippets)
        val failMsg = failures.map(failure => compareError(source, generated, failure)).mkString(EOL)
        result(failures.isEmpty, "Contains all", failMsg, expectable)
      case CompileFailure(source, msgs) => MatchFailure("", compileError(source, msgs), expectable)
    }
  }
}
