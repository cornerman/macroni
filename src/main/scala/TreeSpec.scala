package macroni

import macroni.matcher._

import scala.reflect.runtime.universe.Tree
import org.specs2.mutable.Specification
import org.specs2.matcher.Matcher

trait TreeSpec extends Specification with TreeMatchers {
  implicit def StringToTreeMatcher(matcher: Matcher[String]): Matcher[Tree] = new TreeStringMatcher(matcher)
  implicit def TreeToMatcher(tree: Tree): Matcher[Tree] = haveTree(tree)
}
