package example

import macroni.CompileSpec
import macroni.compiler._
import scala.reflect.runtime.universe._

class HelloSpec extends CompileSpec {

  "simple hello compiles to" >> {
    q"""@example.hello object A""" must compile.to(
      q"""object A { def hello: String = "hello" }"""
    )
  }

  "simple hello compiles containing" >> {
    q"""@example.hello object A""" must compile.to(
      q"""def hellol: String""" or not(q"""Int""")
      // not(contain(q"""String"""))
    )
  }

  "simple hello compiles" >> {
    q"object A" must macroni.matcher.TreeMatchers.beEqualTo(q"object A")
    q"@example.hello object A" must compile
    q"@example.hello object A" must not(abort)
    q"@example.hello object A" must compile.canWarn
  }

  "duplicate hello doesn't compile" >> {
    q"@example.hello object A { val hello = 1 }" must abort
    q"@example.hello object A { val hello = 1 }" must not(compile)
    q"@example.hello object A { val hello = 1 }" must compile.withError
    q"@example.hello object A { val hello = 1 }" must abort(startWith("reflective typecheck has failed: method hello is defined twice"))
    q"@example.hello object A { val hello = 1 }" must compile.withError(startWith("reflective typecheck has failed: method hello is defined twice"))

    q"@example.hello object A { val hello = 1 }" must compile.withErrors(startWith("reflective typecheck has failed: method hello is defined twice"), startWith("<") and endWith(">"), "asd")
  }

  "detect warning" >> {
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must warn
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.canWarn
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.withWarning
    println(Compiler(q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }").asInstanceOf[CompileSuccess].generated.collect { case a => a })
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.withWarning(contain("erasure")).to(contain(q"""def hello: String = "hello""""))
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must warn("abstract type T is unchecked since it is eliminated by erasures")
  }

  "detect warning" >> {
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must warn
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.canWarn
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.withWarning
    println(Compiler(q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }").asInstanceOf[CompileSuccess].generated.children)
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must compile.withWarning(contain("erasure")).to(haveChild(q"foobar"))
    q"@example.hello object A { def bar[T](l: T) = 1.isInstanceOf[T] }" must warn("abstract type T is unchecked since it is eliminated by erasures")
  }

  "detect error" >> {
    q"@example.hello object A { val foo: String = 2 }" must abort
    q"@example.hello object A { val foo: String = 2 }" must compile.withError
    q"@example.hello object A { val foo: String = 2 }" must abort(contain("type mismatch"))

    q"@example.hello object A { val foo: String = 2 }" must abort()
  }

  "detect abort in macro" >> {
    q"@example.hello class A" must abort
    q"@example.hello class A" must compile.withError
    q"@example.hello class A" must abort("reflective typecheck has failed: Cannot match object pattern")
    q"@example.hello class A" must compile.withError("reflective typecheck has failed: Cannot match object pattern")
  }
}
