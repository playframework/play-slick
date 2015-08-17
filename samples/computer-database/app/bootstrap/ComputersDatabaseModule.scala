package bootstrap

import com.google.inject.AbstractModule

class ComputersDatabaseModule extends AbstractModule {
  protected def configure: Unit = {
    bind(classOf[InitialData]).asEagerSingleton()
  }
}
