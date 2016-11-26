package example

import macroni.{CompileSpec, TreeSpec, ContextMock}

import scala.reflect.macros.blackbox

class GoodbyeTranslatorSpec extends TreeSpec with ContextMock {
  val context = mockContext[blackbox.Context]
  import context.universe._

  val translator = GoodbyeTranslator(context)

  "goodbye translator translates" >> {
    translator.translate(q"""println("bye")""") must beEqualToTree(
      q"""new example.Goodbye {
        def make() {
          println("bye")
          System.exit(0)
        }
      }"""
    )
  }
}

class GoodbyeSpec extends CompileSpec {
  import scala.reflect.runtime.universe._

  "bad goodbye does not compile" >> {
    q"example.goodbye(unknown)" must abort
  }

  "simple goodbye compiles to" >> {
    q"def f {}; example.goodbye(f)" must compile.to(containTree(q"f", q"0"))
  }
}
