package play.api.db.slick.evolutions

import org.specs2.mutable.Specification
import play.api.db.DBApi
import play.api.db.slick.TestData
import play.api.db.slick.evolutions.internal.DBApiAdapter
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.db.slick.util.WithReferenceConfig

class EvolutionsModuleSpec extends Specification {

  "reference.conf" should {
    "evolutions module is enabled" in new WithReferenceConfig {
      enabledModules(ref) must contain(classOf[EvolutionsModule].getName)
    }
  }

  "EvolutionsModule" should {
    val appBuilder = new GuiceApplicationBuilder(configuration = (TestData.configuration))
    val injector = appBuilder.injector()

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
}