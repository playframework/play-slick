package bootstrap

import com.google.inject.AbstractModule

class ComputersDatabaseModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[InitialData]).asEagerSingleton()
  }
}
