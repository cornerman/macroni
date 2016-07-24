package example

import macroni.{CompileSpec, TreeSpec, ContextMock}

class GoodbyeTranslatorSpec extends TreeSpec with ContextMock {
  import blackboxContext.universe._

  val translator = GoodbyeTranslator(blackboxContext)

  "simple hello compiles to" >> {
    translator.translate(q"""println("bye")""") must beEqualTo(
      q"""new example.Goodbye {
        def make() {
          scala.Predef.println("bye")
          java.lang.System.exit(0)
        }
      }"""
    ).orPending
  }
}

class GoodbyeSpec extends CompileSpec {
  import scala.reflect.runtime.universe._

  "bad goodbye does not compile" >> {
    q"example.goodbye(unknown)" must abort
  }

  "simple goodbye compiles to" >> {
    q"def f {}; example.goodbye(f)" must compile.to(contain(q"f", q"0"))
  }
}
