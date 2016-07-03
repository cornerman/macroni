package macroni.compare

import scala.reflect.runtime.universe.Tree

trait ExpectedCode { val code: Tree }
case class With(code: Tree) extends ExpectedCode
case class Not(code: Tree) extends ExpectedCode
