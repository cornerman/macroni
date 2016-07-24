import macroni.CompileSpec
import macroni.compiler._
import macroni.matcher._
import macroni.helper._

import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import org.specs2.matcher._

class CompileMatcherSpec extends CompileSpec {
  val matchFailureExpTag = ClassTag(classOf[MatchFailureException[_]])

  def failMatcher(createMatcher: String => Matcher[String])(msg: String): Matcher[Any] = throwA(matchFailureExpTag).like {
    case e: MatchFailureException[_] =>
      val matcher = createMatcher(Colors.unhighlight(msg))
      matcher(createExpectable(Colors.unhighlight(e.getMessage)))
  }

  val failMatch = failMatcher(s => AnyMatchers.beEqualTo(s)) _
  val failMatchLike = failMatcher(s => StringMatchers.matching("(.|\\n)*".r)) _

  val info = Info(NoPosition, "some info")
  val warning = Warning(NoPosition, "some warning")
  val error = Error(NoPosition, "some error")

  "success compiles" >> {
    CompileSuccess(q"", q"", Seq.empty) must compile
  }

  "success with info compiles" >> {
    CompileSuccess(q"", q"", Seq(info)) must compile
  }

  "success with warnings compiles with warnings" >> {
    CompileSuccess(q"", q"", Seq(warning)) must not(compile)
    CompileSuccess(q"", q"", Seq(warning)) must compile.withWarning("some warning")
  }

  "success abort fails" >> {
    val src = q"object A"
    val success = CompileSuccess(src, src, Seq.empty)
    (success must abort) must failMatch(FailDescription.shouldNotCompile(success))
  }

  "success with warning mismatch fails" >> {
    val src = q"object A"
    val success = CompileSuccess(src, src, Seq.empty)
    (success must compile.withWarning("meh")) must failMatchLike(FailDescription.compilerWarnings(src, "(.|\n)*"))
  }

  "success with less warnings fails" >> {
    val src = q"object A"
    val success = CompileSuccess(src, src, Seq(warning))
    (success must compile.withWarnings("some warning", "some warning")) must failMatchLike(FailDescription.compilerWarnings(src, "(.|\n)*"))
  }

  "success with more warnings fails" >> {
    val src = q"object A"
    val success = CompileSuccess(src, src, Seq(warning, warning))
    (success must compile.withWarning("some warning")) must failMatchLike(FailDescription.compilerWarnings(src, "(.|\n)*"))
  }

  "success compiles to" >> {
    val gen = q"object A { def i = 1 }"
    val success = CompileSuccess(q"object A", gen, Seq.empty)
    success must compile.to(gen)
  }

  "failure aborts" >> {
    CompileFailure(q"", Seq(error), Seq.empty) must abort
  }

  "failure with info aborts" >> {
    CompileFailure(q"", Seq(error), Seq(info)) must abort
  }

  "failure with warnings aborts with warnings" >> {
    CompileFailure(q"", Seq(error), Seq(warning)) must not(abort)
    CompileFailure(q"", Seq(error), Seq(warning)) must abort.withWarning("some warning")
  }

  "failure without errors neither compiles nor aborts" >> {
    CompileFailure(q"", Seq.empty, Seq.empty) must not(abort)
    CompileFailure(q"", Seq.empty, Seq.empty) must not(compile)
  }

  "failure compile fails" >> {
    val failure = CompileFailure(q"object A", Seq(error), Seq.empty)
    (failure must compile) must failMatch(FailDescription.shouldCompile(failure))
  }

  "failure with warning mismatch fails" >> {
    val src = q"object A"
    val failure = CompileFailure(src, Seq(error), Seq.empty)
    (failure must abort.withWarning("meh")) must failMatchLike(FailDescription.compilerErrors(src, "(.|\n)*"))
  }

  "failure with less warnings fails" >> {
    val src = q"object A"
    val failure = CompileFailure(src, Seq(error), Seq(warning))
    (failure must abort.withWarnings("some warning", "some warning")) must failMatchLike(FailDescription.compilerErrors(src, "(.|\n)*"))
  }

  "failure with more warnings fails" >> {
    val src = q"object A"
    val failure = CompileFailure(src, Seq(error), Seq(warning, warning))
    (failure must abort.withWarning("some warning")) must failMatchLike(FailDescription.compilerErrors(src, "(.|\n)*"))
  }

  "failure with error mismatch fails" >> {
    val src = q"object A"
    val failure = CompileFailure(src, Seq(error), Seq.empty)
    (failure must compile.withError("meh")) must failMatchLike(FailDescription.compilerErrors(src, "(.|\n)*"))
  }

  "failure with less errors fails" >> {
    val src = q"object A"
    val failure = CompileFailure(src, Seq(error), Seq.empty)
    (failure must abort.withErrors("some error", "some error")) must failMatchLike(FailDescription.compilerErrors(src, "(.|\n)*"))
  }

  "failure with more errors fails" >> {
    val src = q"object A"
    val failure = CompileFailure(src, Seq(error, error), Seq.empty)
    (failure must abort.withError("some error")) must failMatchLike(FailDescription.compilerErrors(src, "(.|\n)*"))
  }

  "failure compiles to fails" >> {
    val src = q"object A"
    val failure = CompileFailure(src, Seq(error), Seq.empty)
    (failure must compile.to(src)) must failMatch(FailDescription.shouldCompile(failure))
  }
}
