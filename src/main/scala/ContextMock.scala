package macroni

import org.specs2.matcher.Matcher
import org.specs2.mock.Mockito
import org.specs2.mock.mockito.MocksCreation
import scala.reflect.{ClassTag, macros, runtime}

import matcher.{TreeMatchers, FakeTreeMatcher}

trait MockedContext[C <: macros.blackbox.Context] extends Mockito {
  val context: C

  import context.universe.Tree

  implicit def TreeToTree(tree: Tree) = tree.asInstanceOf[runtime.universe.Tree]
  implicit def TreeMatcherToTreeMatcher(matcher: Matcher[runtime.universe.Tree]) = new FakeTreeMatcher[Tree](matcher)
}

object MockedContext extends Mockito {
  def apply[C <: macros.blackbox.Context : ClassTag]: C = {
    val mocked = MocksCreation.smartMock[C]
    mocked.universe returns runtime.universe.asInstanceOf[macros.Universe]
    mocked
  }
}

object whitebox {
  import macros.whitebox.Context
  trait ContextMock extends MockedContext[Context] {
    val context = MockedContext[Context]
  }
}

object blackbox {
  import macros.blackbox.Context
  trait ContextMock extends MockedContext[Context] {
    val context = MockedContext[Context]
  }
}
