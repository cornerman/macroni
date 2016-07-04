package macroni.matcher

import org.specs2.matcher._

class SeqMatcherOfMatchers[T](matchers: Seq[Matcher[T]]) extends Matcher[Seq[T]] {
  import scala.compat.Platform.EOL

  override def apply[S <: Seq[T]](expectable: Expectable[S]): MatchResult[S] = {
    val missingMatchers = matchers.drop(expectable.value.size)
    val missingValues = expectable.value.drop(matchers.size) // TODO exactly without order semantic?
    val results: Seq[MatchResult[T]] = matchers.zip(expectable.value).map { case (matcher, exp) =>
      matcher.apply(createExpectable(exp))
    }

    def failure[M](reason: String, missing: Seq[_])(m: M): MatchFailure[M] = {
      val misses = missing.mkString(EOL)
      MatchFailure("", reason + ":" + EOL + misses, createExpectable(m))
    }

    val missingResults = missingMatchers.map(failure("Got less than expected, missing", missingMatchers)) ++ missingValues.map(failure("Got more than expected", missingValues))

    result(MatchResult.sequence(results ++ missingResults), expectable)
  }
}

class NamedMatcher[T](name: String, matcher: Matcher[T]) extends Matcher[T] {
  override def apply[S <: T](expectable: Expectable[S]): MatchResult[S] = matcher(expectable)
  override def toString = name
}
