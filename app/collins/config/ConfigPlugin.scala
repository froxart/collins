package collins.config

import play.api.{Application, Plugin}
import collins.util.config.Registry

class ConfigPlugin(app: Application) extends Plugin {

  override def enabled = true

  override def onStart() {
    if (enabled) {
      Registry.initializeAll(app)
      Registry.validate
    }
  }

  override def onStop() {
    Registry.shutdown()
  }

}
