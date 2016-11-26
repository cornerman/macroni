# macroni

Test your scala macros with specs2

Provides specs2 matchers and helpers for compiling/matching trees as well as mocking the macro context in tests.

*Still work in progress.*

## Usage

First publish macroni locally:
```
$ sbt publish-local
```

Then use it in your own project:
```
libraryDependencies += "com.github.cornerman" %% "macroni" % "0.0.1-SNAPSHOT" % "test"
```

### Tree Matchers

Matching trees with an equality-based matcher does not really work and using purely string-based matchers can be error-prone.
The provided tree matchers can be used like this:
```scala
import macroni._
import scala.reflect.runtime.universe._

class CodeSpec extends TreeSpec {
  "tree equals same tree" >> {
    val treeA = q"object A { def pi = 1 }"
    val treeB = q"object A { def pi = 1 }"
    treeA should beEqualToTree(treeB)
  }

  "tree contains its subtree" >> {
    val tree = q"object A { def pi = 1 }; 2"
    tree should containTree(q"def pi = 1", q"2")
  }

  "tree has its direct children" >> {
    val tree = q"def pi = 1; def es = 2"
    tree should haveChildTree(q"def pi = 1", q"def es = 2")
  }

}
```

### Context Mock

Say, you wrote a method operating on some context type (blackbox or whitebox):
```scala
import scala.reflect.macros.blackbox

def translate(c: blackbox.Context)(tree: c.Tree): c.Tree
```

In unit tests, you want to test such a context-dependent function.
Therefore, you will need a context mock (blackbox or whitebox):
```scala
import macroni._
import scala.reflect.macros.blackbox

class ContextMockSpec extends TreeSpec with ContextMock {
  "blackbox context tree equals same tree" >> {
    val context = mockContext[blackbox.Context]
    import context.universe._

    translate(context)(q"true") should beEqualToTree(q"val golum = true")
  }
}
```

### Compile Matchers

Sometimes you might want to compile the generated code (including macro expansion) to check for sanity.
Here, You can use a compiling tree matcher:
```scala
import macroni._
import scala.reflect.runtime.universe._

class CompilingCodeSpec extends CompileSpec {
  "simple hello compiles to" >> {
    q"@hello object A" must compile.to(
      q"""object A { def hello: String = "hello" }"""
    )
  }

  "simple hello compiles containing" >> {
    q"@hello object A" must compile.to(
      containTree(q"String") and not(containTree(q"Int")),
      haveChildTree(haveChildTree(q"""def hello: String = "hello""""))
    )
  }

  "wrong hello aborts" >> {
    q"@hello object A { val foo: String = 2 }" must abort(contain("type mismatch"))
  }
}

```
