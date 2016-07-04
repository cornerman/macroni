package example

import macroni.CompileSpec
import scala.reflect.runtime.universe._

class HelloSpec extends CompileSpec {

  "simple hello compiles to" >> {
    q"""@example.hello object A""" must compile.to(
      q"""object A { def hello: String = "hello" }"""
    )
  }

  "simple hello compiles containing" >> {
    q"""@example.hello object A""" must compile.containing(
      q"""def hello: String""",
      not(q"""Int""")
    )
  }

  "simple hello compiles" >> {
    q"@example.hello object A" must compile
    q"@example.hello object A" must not(abort)
    q"@example.hello object A" must compile.canWarn
  }

  "duplicate hello doesn't compile" >> {
    q"@example.hello object A { val hello = 1 }" must abort
    q"@example.hello object A { val hello = 1 }" must not(compile)
    q"@example.hello object A { val hello = 1 }" must abort(startWith("reflective typecheck has failed: method hello is defined twice"))
    q"@example.hello object A { val hello = 1 }" must compile.withError
  }

  "detect warning" >> {
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must warn
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.canWarn
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must warn("abstract type T is unchecked since it is eliminated by erasure")
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.withWarning
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.withWarning.containing(q"object A")
  }

  "detect error" >> {
    q"@example.hello object A { val foo: String = 2 }" must abort
    q"@example.hello object A { val foo: String = 2 }" must abort(contain("type mismatch"))
    q"@example.hello object A { val foo: String = 2 }" must compile.withError
  }

  "detect abort in macro" >> {
    q"@example.hello class A" must abort
    q"@example.hello class A" must abort("reflective typecheck has failed: Cannot match object pattern")
    q"@example.hello class A" must compile.withError
  }
}
