package macroni

import org.specs2.mock.Mockito

import scala.reflect.macros.{blackbox, whitebox}
import scala.reflect.macros.contexts.Context
import scala.reflect.macros
import scala.reflect.runtime

trait ContextMock extends Mockito {
  val whiteboxContext: whitebox.Context = {
    val context = mock[whitebox.Context].smart
    context.universe returns runtime.universe.asInstanceOf[macros.Universe]
    context
  }

  val blackboxContext: blackbox.Context = whiteboxContext

  implicit def whiteTreeToTree(tree: whiteboxContext.universe.Tree) = tree.asInstanceOf[runtime.universe.Tree]
  implicit def blackTreeToTree(tree: blackboxContext.universe.Tree) = tree.asInstanceOf[runtime.universe.Tree]

}
