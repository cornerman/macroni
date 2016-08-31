import macroni.{blackbox, whitebox, TreeSpec}
import macroni.matcher._

import org.specs2.matcher._

class BlackboxContextMockSpec extends TreeSpec with blackbox.ContextMock {
  "backbox context tree equals same tree" >> {
    import context.universe._

    val tree = q"val golum = true"
    tree should haveTree(q"val golum = true")
  }
}

class WhiteboxContextMockSpec extends TreeSpec with whitebox.ContextMock {
  "whitebox context tree equals same tree" >> {
    import context.universe._

    val tree = q"val golum = true"
    tree should haveTree(q"val golum = true")
  }
}
