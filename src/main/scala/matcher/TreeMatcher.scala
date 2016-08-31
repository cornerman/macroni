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

class FakeTreeMatcher[T](matcher: Matcher[Tree]) extends Matcher[T] {
  override def apply[S <: T](expectable: Expectable[S]): MatchResult[S] = {
    matcher.apply(expectable.asInstanceOf[Expectable[Tree]]).asInstanceOf[MatchResult[S]]
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

  val matcher: Matcher[Seq[Tree]]
  val children: Tree => Seq[Tree]
  val describeFail: (Tree, String) => String

  override def apply[S <: Tree](expectable: Expectable[S]): MatchResult[S] = {
    val seqExpectable = createExpectable(children(expectable.value))
    val matched = matcher(seqExpectable)
    result(matched, expectable).updateMessage(describeFail(expectable.value, _))
  }
}

class TreeChildrenMatcher(val matcher: Matcher[Seq[Tree]]) extends TreeChildMatcher {
  val children = (tree: Tree) => tree.children
  val describeFail = FailDescription.shouldHaveChild _
}

class TreeDescendantsMatcher(val matcher: Matcher[Seq[Tree]]) extends TreeChildMatcher {
  val children = (tree: Tree) => tree.collect { case t => t }
  val describeFail = FailDescription.shouldContain _
}

trait TreeMatchers {
  private def newTreeChildMatcher(matchers: Seq[Matcher[Tree]], construct: Matcher[Seq[Tree]] => Matcher[Tree]): Matcher[Tree] = {
    matchers.map { matcher =>
      val check = ValueChecks.matcherIsValueCheck(matcher)
      construct(TraversableMatchers.contain(check))
    }.fold(new AlwaysMatcher[Tree])((a,b) => a and b)
  }

  def haveTree(tree: Tree) = new TreeStringMatcher(new BeEqualTo(showCode(tree)))
  def haveChild(matchers: Matcher[Tree]*) = newTreeChildMatcher(matchers, m => new TreeChildrenMatcher(m))
  def haveDescendant(matchers: Matcher[Tree]*) = newTreeChildMatcher(matchers, m => new TreeDescendantsMatcher(m))
  def haveChild(matcher: Matcher[Seq[Tree]]) = new TreeChildrenMatcher(matcher)
  def haveDescendant(matcher: Matcher[Seq[Tree]]) = new TreeDescendantsMatcher(matcher)
}

object TreeMatchers extends TreeMatchers
