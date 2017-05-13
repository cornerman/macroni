import macroni.{ContextMock, TreeSpec, CompileSpec}
import macroni.matcher._

import org.specs2.mock.Mockito
import scala.reflect.macros.{blackbox, whitebox}
import org.specs2.matcher._

class ContextMockSpec extends TreeSpec with ContextMock with CompileSpec {
  "blackbox context tree equals same tree" >> {
    val context = mockContext[blackbox.Context]
    import context.universe._

    val tree = q"val golum = true"
    tree should beEqualToTree(q"val golum = true")
  }

  "whitebox context tree equals same tree" >> {
    val context = mockContext[whitebox.Context]
    import context.universe._

    val tree = q"val golum = true"
    tree should beEqualToTree(q"val golum = true")
  }

  "mocked context with compile matcher" >> {
    val context = mockContext[whitebox.Context]
    import context.universe._

    q"1" must compile
  }
}
