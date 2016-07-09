package macroni.compiler

import scala.reflect.internal.util.Position
import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe.Tree
import scala.tools.reflect.{FrontEnd, ToolBox}
import scala.util.{Try, Success, Failure}

class Reporter extends FrontEnd {
  //TODO: needed because FrontEnd stores `infos` in a LinkedHashSet.
  // Thus, messages with the same hashcode are lost. This can happen
  // in quasiquotes, where the position may be NoPosition.
  val infoList = new scala.collection.mutable.ArrayBuffer[Info]

  override def log(pos: Position, msg: String, severity: Severity) {
    super.log(pos, msg, severity)
    infoList += Info(pos, msg, severity)
  }

  override def reset() {
    super.reset()
    infoList.clear()
  }

  def display(info: Info) {}
  def interactive() {}
}

object Config {
  private val paradisePath = System.getProperty("user.home") + "/.ivy2/cache/org.scalamacros/paradise_2.11.8/jars/paradise_2.11.8-2.1.0.jar"
  private val paradiseJar = if (new java.io.File(paradisePath).exists) Some(paradisePath) else None
  private val paradiseOptions = paradiseJar.map(path => s"-Xplugin-require:macroparadise -Xplugin:$path").getOrElse("")

  val options = paradiseOptions
}

object Compiler {
  def apply(tree: Tree): CompileResult = {
    val reporter = new Reporter
    val toolbox = currentMirror.mkToolBox(reporter, Config.options)

    Try(toolbox.typecheck(tree)) match {
      case Success(typedTree) => CompileResult(tree, typedTree, reporter)
      case Failure(e) => CompileResult(tree, e, reporter)
    }
  }
}
