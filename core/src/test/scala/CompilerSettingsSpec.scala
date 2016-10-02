import macroni.CompileSpec
import scala.reflect.runtime.universe._

class CompilerSettingsSpec extends CompileSpec {

  "compiler settings in test" >> {
    macroni.macros.CompilerSettings() must contain(
      startWith("-Xplugin:") and endWith(".ivy2/cache/org.scalamacros/paradise_2.11.8/jars/paradise_2.11.8-2.1.0.jar")
    )
  }

  "compiler settings in tree" >> {
    q"macroni.macros.CompilerSettings()" must compile.to(containTree(
      startWith("\"-Xplugin:") and endWith(".ivy2/cache/org.scalamacros/paradise_2.11.8/jars/paradise_2.11.8-2.1.0.jar\"")
    ))
  }
}
