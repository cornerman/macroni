package helpers

import reflect.macros.blackbox.Context

class ContextTools[C <: Context](val context: C) {
  import context.universe._

  def mkList[I,R](values: List[I])(implicit lift: Liftable[I]): context.Expr[List[R]] = context.Expr(q"List(..$values)")

  def namedValue(name: String, value: context.Tree): context.Tree = {
    q"helpers.NamedValue($name, $value)"
  }

  def termToNamedValue(term: context.TermName): context.Tree = {
    val name = term.decodedName.toString
    namedValue(name, q"$term")
  }

  def treeToNamedValue(tree: context.Tree): context.Tree = {
    tree match {
      case v@q"CodeSpec.this.StringToMatcher($actual)" => namedValue(actual.toString, v)
      case v@Select(_, term: TermName) => termToNamedValue(term)
      case v@Ident(term: TermName) => termToNamedValue(term)
      case v: Literal => namedValue(v.toString, v)
      case v => namedValue(v.toString, v)
    }
  }
}

object ContextTools {
  def apply(c: Context): ContextTools[c.type] = new ContextTools(c)
}
