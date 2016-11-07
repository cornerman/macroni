package macroni

import org.specs2.matcher.Matcher
import org.specs2.mock.Mockito
import org.specs2.mock.mockito.MocksCreation
import scala.reflect.{ClassTag, runtime}
import scala.reflect.macros.{blackbox => macroBlackbox, whitebox => macroWhitebox, Universe => macroUniverse}

trait MockedContext[C <: macroBlackbox.Context] {
  val context: C

  import context.universe.Tree
  import matcher.FakeTreeMatcher

  implicit def TreeToTree(tree: Tree) = tree.asInstanceOf[runtime.universe.Tree]
  implicit def TreeMatcherToTreeMatcher(matcher: Matcher[runtime.universe.Tree]) = new FakeTreeMatcher[Tree](matcher)
}

object MockedContext extends Mockito {
  def apply[C <: macroBlackbox.Context : ClassTag]: C = {
    val mocked = MocksCreation.smartMock[C]
    mocked.universe returns runtime.universe.asInstanceOf[macroUniverse]
    mocked
  }
}

object whitebox {
  import macroWhitebox.Context
  trait ContextMock extends MockedContext[Context] {
    val context = MockedContext[Context]
  }
}

object blackbox {
  import macroBlackbox.Context
  trait ContextMock extends MockedContext[Context] {
    val context = MockedContext[Context]
  }
}
