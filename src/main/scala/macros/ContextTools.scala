package macroni.macros

import reflect.macros.blackbox.Context

class ContextTools[C <: Context](val context: C) {
  import context.universe._

  def mkList[I,R](values: List[I])(implicit lift: Liftable[I]): context.Expr[List[R]] = context.Expr(q"List(..$values)")

  def namedValue(name: String, value: context.Tree): context.Tree = {
    q"macroni.macros.NamedValue($name, $value)"
  }

  def matcherNameFromTree(tree: context.Tree): String = tree match {
    case v@q"$_.this.StringToMatcher($actual)" => matcherNameFromTree(actual)
    case v@q"{..$prefix}.$func($actual)" => s"$func(${matcherNameFromTree(actual)})"
    case v => v.toString
  }

  def termToNamedValue(term: context.TermName): context.Tree = namedValue(term.toString, q"$term")
  def treeToNamedValue(tree: context.Tree): context.Tree = namedValue(matcherNameFromTree(tree), tree)
}

object ContextTools {
  def apply(c: Context): ContextTools[c.type] = new ContextTools(c)
}
