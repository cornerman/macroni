import macroni.TreeSpec
import macroni.compiler._
import macroni.matcher._
import macroni.helper._

import scala.reflect.runtime.universe._
import org.specs2.matcher._

class TreeMatcherSpec extends TreeSpec {

  "tree equals itself" >> {
    val tree = q"object A { def pi = 1 }"
    tree should beEqualTo(tree)
  }

  "tree does not equal something else" >> {
    val treeA = q"object A { def pi = 1 }"
    val treeB = q"object A { def pi = 2 }"
    treeA should not(beEqualTo(treeB))
  }

  "tree contains its subtree" >> {
    val tree = q"object A { def pi = 1 }; 2"

    tree should contain(
      q"def pi = 1",
      q"1",
      q"2"
    )
  }

  "tree in string is not contained" >> {
    val tree = q"object A { def pi = 1 }; 2"

    tree should contain(
      q"def pi = 1",
      q"1",
      q"2"
    )
  }

  "tree does not contain other subtrees" >> {
    val tree = q"object A { def pi = 1 }"

    tree should not(contain(
      q"def pi = 1",
      q"bier",
      q"2"
    ))
  }

  "tree has its direct children" >> {
    val tree = q"def pi = 1; def es = 2"

    tree should haveChild(
      q"def pi = 1",
      q"def es = 2"
    )
  }

  "tree has exactly its direct children" >> {
    val tree = q"def pi = 1; def es = 2; List(pi, es)"

    //TODO: without explicit TreeMatchers.beEqualTo
    tree should haveChild(exactly(
      beEqualTo(q"def pi = 1"),
      beEqualTo(q"def es = 2"),
      beEqualTo(q"List(pi, es)")
    ))
  }

  "tree does not have other direct children" >> {
    val tree = q"def pi = 1; def es = 2"

    tree should not(haveChild(
      q"def pi = 1",
      q"2"
    ))
  }
}
