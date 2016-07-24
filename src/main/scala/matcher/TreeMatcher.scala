package macroni.matcher

import macroni.compiler.{CompileResult, CompileSuccess, CompileFailure}
import macroni.helper.FailDescription

import org.specs2.matcher._
import scala.compat.Platform.EOL
import scala.reflect.runtime.universe.{showCode, Tree}

class CompiledTreeMatcher(matcher: Matcher[Tree]) extends Matcher[CompileResult] {
  override def apply[S <: CompileResult](expectable: Expectable[S]): MatchResult[S] = {
    expectable.value match {
      case CompileSuccess(_, gen, _) => result(matcher(createExpectable(gen)), expectable)
      case c: CompileFailure => failure(FailDescription.shouldCompile(c), expectable)
    }
  }
}

class TreeStringMatcher(matcher: Matcher[String]) extends Matcher[Tree] {
  override def apply[S <: Tree](expectable: Expectable[S]): MatchResult[S] = {
    val matched = matcher(createExpectable(showCode(expectable.value)))
    result(matched, expectable)
  }
}

trait TreeChildMatcher extends Matcher[Tree] {
  import MatchResultCombinators.MatchResultCombinator
  import scala.reflect.runtime.universe._

  val matcher: Matcher[Tree]
  val children: Tree => Seq[Tree]
  val describeFail: (Tree, String) => String

  override def apply[S <: Tree](expectable: Expectable[S]): MatchResult[S] = {
    val check = ValueChecks.matcherIsValueCheck(matcher)
    val seqMatcher = TraversableMatchers.contain(check)
    val seqExpectable = createExpectable(children(expectable.value))
    val matched = seqMatcher(seqExpectable)
    result(matched, expectable).updateMessage(describeFail(expectable.value, _))
  }
}

class TreeHasChildMatcher(val matcher: Matcher[Tree]) extends TreeChildMatcher {
  val children = (tree: Tree) => tree.children
  val describeFail = FailDescription.shouldHaveChild _
}

class TreeContainsMatcher(val matcher: Matcher[Tree]) extends TreeChildMatcher {
  val children = (tree: Tree) => tree.collect { case t => t }
  val describeFail = FailDescription.shouldContain _
}

trait TreeMatchers {
  def beEqualTo(tree: Tree) = new TreeStringMatcher(new BeEqualTo(showCode(tree)))
  def haveChild(matchers: Matcher[Tree]*) = matchers.map(m => new TreeHasChildMatcher(m)).fold(new AlwaysMatcher[Tree])((a,b) => a and b)
  def contain(matchers: Matcher[Tree]*) = matchers.map(m => new TreeContainsMatcher(m)).fold(new AlwaysMatcher[Tree])((a,b) => a and b)
}

object TreeMatchers extends TreeMatchers
