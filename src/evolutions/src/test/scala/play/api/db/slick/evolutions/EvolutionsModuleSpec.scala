package play.api.db.slick.evolutions

import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.Environment
import play.api.db.DBApi
import play.api.db.slick.SlickComponents
import play.api.db.slick.TestData
import play.api.db.slick.evolutions.internal.DBApiAdapter
import play.api.inject.ApplicationLifecycle
import play.api.inject.DefaultApplicationLifecycle
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.db.slick.util.WithReferenceConfig

import scala.concurrent.ExecutionContext

class EvolutionsModuleSpec extends Specification {

  "reference.conf" should {
    "evolutions module is enabled" in new WithReferenceConfig {
      enabledModules(ref) must contain(classOf[EvolutionsModule].getName)
    }
  }

  "EvolutionsModule" should {
    val appBuilder = GuiceApplicationBuilder(configuration = TestData.configuration)
    val injector   = appBuilder.injector()

    "bind DBApi to DBApiAdapter" in {
      val api = injector.instanceOf[DBApi]
      api must beAnInstanceOf[DBApiAdapter]
    }
    "bind DBApi as a singleton" in {
      val api1 = injector.instanceOf[DBApi]
      val api2 = injector.instanceOf[DBApi]
      api1 mustEqual api2
    }
  }

  "SlickEvolutionsComponents" should {
    object TestComponents extends SlickComponents with SlickEvolutionsComponents {
      override def environment: Environment = Environment.simple()

      override def applicationLifecycle: ApplicationLifecycle = new DefaultApplicationLifecycle

      override def configuration: Configuration = TestData.configuration

      override def executionContext: ExecutionContext =
        ExecutionContext.Implicits.global // using the global EC since this is test code
    }

    "bind DBApi to DBApiAdapter" in {
      val api = TestComponents.dbApi
      api must beAnInstanceOf[DBApiAdapter]
    }
    "bind DBApi as a singleton" in {
      val api1 = TestComponents.dbApi
      val api2 = TestComponents.dbApi
      api1 mustEqual api2
    }
  }
}
