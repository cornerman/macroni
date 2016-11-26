package macroni

import scala.reflect.runtime.universe.Tree
import org.specs2.mutable.Specification
import org.specs2.matcher.Matcher

import macroni.matcher.{TreeStringMatcher, TreeMatchers, CovertTreeMatcher}

trait TreeSpec extends Specification with TreeMatchers {
  implicit def StringToTreeMatcher(matcher: Matcher[String]): Matcher[Tree] = new TreeStringMatcher(matcher)
  implicit def TreeToMatcher[T <% Tree](tree: T): Matcher[Tree] = {
    val impl = implicitly[T => Tree]
    beEqualToTree(impl(tree))
  }
  implicit def TreeMatcherToMatcher[T <% Tree](matcher: Matcher[Tree]): Matcher[T] = new CovertTreeMatcher(matcher, implicitly[T => Tree])
}
