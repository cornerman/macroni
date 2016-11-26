package macroni

import org.specs2.matcher.Matcher
import org.specs2.mock.Mockito
import scala.reflect.{ClassTag, runtime}
import scala.reflect.macros.Universe
import scala.reflect.macros.{blackbox,whitebox}

trait ContextMock extends Mockito {
  def mockContext[C <: blackbox.Context : ClassTag]: C = {
    val mocked = mock[C]
    mocked.universe returns runtime.universe.asInstanceOf[Universe]
    mocked
  }

  implicit def MacroToRuntimeTree(tree: Universe#Tree): runtime.universe.Tree = tree.asInstanceOf[runtime.universe.Tree]
}

object ContextMock extends ContextMock
