package test

import core.ApplicationComponents
import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.Around
import org.specs2.specification.Scope
import play.api._
import play.api.test.Helpers

object TestEnvironment {

  val dbTestConf: Configuration = Configuration.from(
    Map(
      "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.default.db.driver" -> "org.h2.Driver",
      "slick.dbs.default.db.url" -> "jdbc:h2:mem:play;MODE=MYSQL",
      "play.evolutions.enabled" -> "true",
      "play.evolutions.autoApply" -> "true"
    )
  )

  def initAppComponents: ApplicationComponents = {
    val context = ApplicationLoader.createContext(
      new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test)
    )
    new ApplicationComponents(context)
  }

  class WithApplicationComponents extends Around with Scope {
    implicit lazy val appComponents = initAppComponents
    implicit lazy val app = appComponents.application

    def around[T: AsResult](t: => T): Result = {
      Helpers.running(app)(AsResult.effectively(t))
    }
  }
}