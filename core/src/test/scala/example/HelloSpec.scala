package example

import macroni.{CompileSpec, TreeSpec, ContextMock}

import scala.reflect.macros.whitebox

class HelloTranslatorSpec extends TreeSpec with ContextMock {
  val context = mockContext[whitebox.Context]
  import context.universe._

  val translator = HelloTranslator(context)

  "hello translator translates" >> {
    translator.translate(q"""object A""") must beEqualToTree(
      q"""object A { def hello: String = "hello" }"""
    )
  }
}

class HelloSpec extends CompileSpec {
  import scala.reflect.runtime.universe._

  "simple hello compiles" >> {
    q"@example.hello object A" must compile
    q"@example.hello object A" must not(abort)
    q"@example.hello object A" must compile.canWarn
  }

  "simple hello compiles to" >> {
    q"""@example.hello object A""" must compile.to(
      q"""object A { def hello: String = "hello" }"""
    )
  }

  "simple hello compiles containing" >> {
    q"""@example.hello object A""" must compile.to(
      containTree(q"""String""") or not(containTree(q"""Int""")),
      haveChildTree(haveChildTree(q"""def hello: String = "hello""""))
    )
  }

  "duplicate hello doesn't compile" >> {
    q"@example.hello object A { val hello = 1 }" must abort
    q"@example.hello object A { val hello = 1 }" must not(compile)
    q"@example.hello object A { val hello = 1 }" must compile.withError
    q"@example.hello object A { val hello = 1 }" must abort(startWith("reflective typecheck has failed: method hello is defined twice"))
    q"@example.hello object A { val hello = 1 }" must compile.withError(startWith("reflective typecheck has failed: method hello is defined twice"))
  }

  "detect warning" >> {
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must warn
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.canWarn
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.withWarning
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must warn(contain("erasure")).to(containTree(q""""hello""""))
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.withWarning(contain("erasure")).to(containTree(q""""hello""""))
  }

  "detect multiple warnings" >> {
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T]; def baz[T](l: T) = 1.isInstanceOf[T] }" must warn
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T]; def baz[T](l: T) = 1.isInstanceOf[T] }" must compile.canWarn
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T]; def baz[T](l: T) = 1.isInstanceOf[T] }" must compile.withWarnings
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T]; def baz[T](l: T) = 1.isInstanceOf[T] }" must warn(contain("erasure"), contain("erasure"))
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T]; def baz[T](l: T) = 1.isInstanceOf[T] }" must compile.withWarnings(contain("erasure"), contain("erasure"))
  }

  "detect error" >> {
    q"@example.hello object A { val foo: String = 2 }" must abort
    q"@example.hello object A { val foo: String = 2 }" must compile.withError
    q"@example.hello object A { val foo: String = 2 }" must abort(contain("type mismatch"))
    q"@example.hello object A { val foo: String = 2 }" must compile.withError(contain("type mismatch"))
  }

  "detect abort in macro" >> {
    q"@example.hello class A" must abort
    q"@example.hello class A" must compile.withError
    q"@example.hello class A" must abort("reflective typecheck has failed: Cannot match object pattern")
    q"@example.hello class A" must compile.withError("reflective typecheck has failed: Cannot match object pattern")
  }
}
