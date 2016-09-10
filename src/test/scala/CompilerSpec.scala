import macroni.compiler._

import scala.reflect.runtime.universe._
import org.specs2.mutable.Specification

class CompilerSpec extends Specification {

  "compile valid code" >> {
    val tree = q"val a = 2"
    Compiler(tree) match {
      case CompileSuccess(source, generated, notices) =>
        (source must beEqualTo(tree)) and (showCode(generated) must beEqualTo(showCode(q"val a = 2"))) and (notices must haveSize(0))
      case CompileFailure(_, _, _) => ko(s"should compile, but doesn't: ${showCode(tree)}")
    }
  }

  "error on invalid code" >> {
    val tree = q"def f: String = 1"
    Compiler(tree) match {
      case CompileSuccess(_, _, _) => ko(s"should not compile, but does: ${showCode(tree)}")
      case CompileFailure(source, errors, notices) =>
        (source must beEqualTo(tree)) and (errors must haveSize(1)) and (notices must haveSize(0))
    }
  }

  "find internal dependency" >> {
    Compiler(q"import macroni._").isInstanceOf[CompileSuccess] must beTrue
  }

  "find external dependency" >> {
    Compiler(q"import org.specs2._").isInstanceOf[CompileSuccess] must beTrue
  }
}
