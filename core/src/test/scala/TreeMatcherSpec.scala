import macroni.TreeSpec

import scala.reflect.runtime.universe._
import org.specs2.matcher._

class TreeMatcherSpec extends TreeSpec {

  "tree equals itself" >> {
    val tree = q"object A { def pi = 1 }"
    tree must beEqualToTree(tree)
  }

  "tree equals same tree" >> {
    val treeA = q"object A { def pi = 1 }"
    val treeB = q"object A { def pi = 1 }"
    treeA should beEqualToTree(treeB)
  }

  "tree does not equal something else" >> {
    val treeA = q"object A { def pi = 1 }"
    val treeB = q"object A { def pi = 2 }"
    treeA should not(beEqualToTree(treeB))
  }

  "tree contains its subtree" >> {
    val tree = q"object A { def pi = 1 }; 2"

    tree should containTree(
      q"def pi = 1",
      q"1",
      q"2"
    )
  }

  "tree in string is not contained" >> {
    val tree = q""""object A { def pi = 1 }""""

    tree should not(containTree(
      q"def pi = 1"
    ))
  }

  "tree does not contain other subtrees" >> {
    val tree = q"object A { def pi = 1 }"

    tree should not(containTree(
      q"def pi = 1",
      q"bier",
      q"2"
    ))
  }

  "tree has its direct children" >> {
    val tree = q"def pi = 1; def es = 2"

    tree should haveChildTree(
      q"def pi = 1",
      q"def es = 2"
    )
  }

  "tree has exactly its direct children" >> {
    val tree = q"def pi = 1; def es = 2; List(pi, es)"

    //TODO: without explicit TreeMatchers.beEqualToTree
    tree should haveChildTree(exactly(
      beEqualToTree(q"def pi = 1"),
      beEqualToTree(q"def es = 2"),
      beEqualToTree(q"List(pi, es)")
    ))
  }

  "tree does not have other direct children" >> {
    val tree = q"def pi = 1; def es = 2"

    tree should not(haveChildTree(
      q"def pi = 1",
      q"2"
    ))
  }
}
