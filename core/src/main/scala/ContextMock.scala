package macroni

import org.specs2.mock.Mockito
import org.specs2.matcher.Matcher
import scala.reflect.{ClassTag, runtime}
import scala.reflect.macros.Universe
import scala.reflect.macros.blackbox
import macroni.compiler.CompileResult
import macroni.matcher.CompilingTreeMatcher

trait ContextMock extends Mockito {
  def mockContext[C <: blackbox.Context : ClassTag]: C = {
    val mocked = mock[C]
    mocked.universe returns runtime.universe.asInstanceOf[Universe]
    mocked
  }

  implicit def MacroToRuntimeTree(tree: Universe#Tree): runtime.universe.Tree = tree.asInstanceOf[runtime.universe.Tree]
  implicit def RuntimeToMacroTreeMatcher[T <% Matcher[runtime.universe.Tree]](matcher: T): Matcher[Universe#Tree] = implicitly[Matcher[runtime.universe.Tree]](matcher).asInstanceOf[Matcher[Universe#Tree]]
}

object ContextMock extends ContextMock
