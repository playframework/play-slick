package play.api.db.slick

import org.specs2.mutable.Specification

import play.api.Configuration
import play.api.PlayException
import play.api.db.evolutions.EvolutionsModule
import play.api.inject.guice.GuiceApplicationBuilder
import slick.profile.BasicProfile

class SlickApiSpec extends Specification {
  trait SUT {
    def config: Configuration
    val appBuilder = {
      new GuiceApplicationBuilder(configuration = config,
        // disabling evolution module as I don't want the databases to be eagerly initialized for these tests
        // (as it would defeat the purpose of the tests)
        disabled = Seq(classOf[EvolutionsModule]))
    }
    val injector = appBuilder.injector()
    val api = injector.instanceOf[SlickApi]
  }

  object SUTWithGoodConfig extends SUT {
    def config: Configuration = TestData.configuration
  }

  object SUTWithBadConfig extends SUT {
    def config: Configuration = TestData.badConfiguration
  }

  "SlickApi.dbConfig" should {
    "return a DatabaseConfig instance for a correctly configured database" in {
      import SUTWithGoodConfig._
      val default = api.dbConfig[BasicProfile](DbName("default"))
      default must not beNull
    }
    "always return the same DatabaseConfig instance for a given database name" in {
      import SUTWithGoodConfig._
      val default1 = api.dbConfig[BasicProfile](DbName("default"))
      val default2 = api.dbConfig[BasicProfile](DbName("default"))
      default1 mustEqual default2
    }
    "throw if a database name doesn't exist in the config" in {
      import SUTWithBadConfig._
      api.dbConfig[BasicProfile](DbName("not-existing")) must throwA[PlayException]
    }
    "throw if a database config doesn't define a Slick driver" in {
      import SUTWithBadConfig._
      api.dbConfig[BasicProfile](DbName("missing-slick-driver")) must throwA[PlayException]
    }
  }

  "SlickApi.dbConfigs" should {
    "return all DatabaseConfig instances for a valid configuration" in {
      import SUTWithGoodConfig._
      api.dbConfigs[BasicProfile] must have size(3)
    }
    "throw if a database name doesn't exist in the config" in {
      import SUTWithBadConfig._
      api.dbConfigs[BasicProfile] must throwA[PlayException]
    }
    "throw if a database config doesn't define a Slick driver" in {
      import SUTWithBadConfig._
      api.dbConfigs[BasicProfile] must throwA[PlayException]
    }
  }
}