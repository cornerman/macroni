package example

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros
import scala.annotation.{StaticAnnotation, compileTimeOnly}

trait Goodbye {
  def make(): Unit
}

class GoodbyeTranslator[C <: Context](val context: C) {
  import context.universe._

  def translate(tree: Tree): Tree = {
    q"""
    new example.Goodbye {
      def make() {
        $tree
        System.exit(0)
      }
    }
    """
  }
}

object GoodbyeTranslator {
  def apply(c: Context): GoodbyeTranslator[c.type] = new GoodbyeTranslator(c)
}

object GoodbyeMacro {
  def impl(c: Context)(arg: c.Expr[Unit]): c.Expr[Goodbye] = {
    val translator = GoodbyeTranslator(c)
    val tree = translator.translate(arg.tree)
    c.Expr[Goodbye](tree)
  }
}

@compileTimeOnly("only for compile time expansion")
object goodbye {
  def apply(arg: Unit): Goodbye = macro GoodbyeMacro.impl
}
