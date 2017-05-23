package macroni

import scala.reflect.runtime.universe.Tree
import org.specs2.mutable.Specification
import org.specs2.matcher.Matcher

import macroni.matcher.{TreeStringMatcher, TreeMatchers}

trait TreeSpec extends Specification with TreeMatchers {
  implicit def StringToTreeMatcher(matcher: Matcher[String]): Matcher[Tree] = new TreeStringMatcher(matcher)
  implicit def TreeToMatcher(tree: Tree): Matcher[Tree] = beEqualToTree(tree)
}
