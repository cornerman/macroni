import macroni.CompileSpec
import scala.reflect.runtime.universe._

class CompilerSettingsSpec extends CompileSpec {
  "compiler settings in test" >> {
    macroni.macros.CompilerSettings() must contain(
      startWith("-Xplugin:") and contain("org.scalamacros/paradise") and endWith(".jar")
    )
  }

  "compiler settings in tree" >> {
    q"macroni.macros.CompilerSettings()" must compile.to(containTree(
      startWith("\"-Xplugin:") and contain("org.scalamacros/paradise") and endWith(".jar\"")
    ))
  }
}
