package macroni.macros

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros
import scala.annotation.compileTimeOnly

object CompilerSettingsMacro {
  def impl(c: Context)(): c.Expr[Seq[String]] = {
    import c.universe._
    c.Expr[Seq[String]](q"${c.compilerSettings}")
  }
}

@compileTimeOnly("only for compile time expansion")
object CompilerSettings {
  def apply(): Seq[String] = macro CompilerSettingsMacro.impl
}
