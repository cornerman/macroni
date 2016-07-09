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
    val matches = children(expectable.value).map(t => matcher(createExpectable(t)))
    val success = matches.find(_.isSuccess).map(result(_, expectable))
    success.getOrElse(failure(describeFail(expectable.value, matcher(createExpectable(q"")).message), expectable))
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
  private def treeHasChild(matchers: Seq[Matcher[Tree]], wrap: Matcher[Tree] => Matcher[Tree]): Matcher[Tree] = {
    matchers.map(wrap).fold(new AlwaysMatcher[Tree])((a,b) => a and b)
  }

  def beEqualTo(tree: Tree) = new TreeStringMatcher(new BeEqualTo(showCode(tree)))
  def haveChild(matchers: Matcher[Tree]*) = treeHasChild(matchers, m => new TreeHasChildMatcher(m))
  def contain(matchers: Matcher[Tree]*) = treeHasChild(matchers, m => new TreeContainsMatcher(m))
}

object TreeMatchers extends TreeMatchers
