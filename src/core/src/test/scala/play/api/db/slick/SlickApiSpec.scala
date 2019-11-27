package play.api.db.slick

import org.specs2.mutable.Specification

import play.api.Configuration
import play.api.PlayException
import play.api.inject.guice.GuiceApplicationBuilder
import slick.basic.BasicProfile

class SlickApiSpec extends Specification {
  trait SUT {
    def config: Configuration
    val appBuilder = new GuiceApplicationBuilder(configuration = config)
    val injector   = appBuilder.injector()
    val api        = injector.instanceOf[SlickApi]
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
    "throw if a database config doesn't define a Slick profile" in {
      import SUTWithBadConfig._
      api.dbConfig[BasicProfile](DbName("missing-slick-profile")) must throwA[PlayException]
    }
  }

  "SlickApi.dbConfigs" should {
    "return all DatabaseConfig instances for a valid configuration" in {
      import SUTWithGoodConfig._
      api.dbConfigs[BasicProfile] must have size (4)
    }
    "throw if a database name doesn't exist in the config" in {
      import SUTWithBadConfig._
      api.dbConfigs[BasicProfile] must throwA[PlayException]
    }
    "throw if a database config doesn't define a Slick profile" in {
      import SUTWithBadConfig._
      api.dbConfigs[BasicProfile] must throwA[PlayException]
    }
  }
}
