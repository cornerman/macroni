package macroni.macros

import reflect.macros.blackbox.Context

case class NamedValue[T](name: String, value: T)

object NamedMatcherMacro {
  def call(func: String)(c: Context)(args: Seq[c.Tree]): c.Tree = {
    val tools = ContextTools(c)
    import c.universe._, tools._
    val values = args.map(treeToNamedValue).toList
    q"new macroni.matcher.SuccessCompileMatcher().${TermName(func)}(${values}.map(nv => new macroni.matcher.NamedMatcher(nv.name, nv.value)): _*)"
  }

  def compileWithWarning(c: Context)(matcher: c.Tree): c.Tree = call("matchWarnings")(c)(Seq(matcher))
  def compileWithError(c: Context)(matcher: c.Tree): c.Tree = call("matchErrors")(c)(Seq(matcher))
  def compileWithWarnings(c: Context)(matchers: c.Tree*): c.Tree = call("matchWarnings")(c)(matchers)
  def compileWithErrors(c: Context)(matchers: c.Tree*): c.Tree = call("matchErrors")(c)(matchers)
}
