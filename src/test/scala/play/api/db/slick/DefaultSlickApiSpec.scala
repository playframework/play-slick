package play.api.db.slick

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import javax.inject.Singleton
import play.api.PlayException
import play.api.db.evolutions.EvolutionsModule
import play.api.inject.ApplicationLifecycle
import play.api.inject.DefaultApplicationLifecycle
import play.api.inject.guice.GuiceApplicationBuilder
import slick.profile.BasicProfile

class DefaultSlickApiSpec extends Specification with Mockito { self =>
  // A new injector should be created to ensure each test is independent of each other
  def injector = {
    val appBuilder = new GuiceApplicationBuilder(configuration = TestData.configuration,
      // disabling evolution module as I don't want the databases to be eagerly initialized for these tests
      // (as it would defeat the purpose of the tests)
      disabled = Seq(classOf[EvolutionsModule]))
    appBuilder.injector()
  }

  def hooks(lifecycle: DefaultApplicationLifecycle): List[_] = {
    val hooksField = lifecycle.getClass.getDeclaredField("hooks")
    hooksField.setAccessible(true)
    hooksField.get(lifecycle).asInstanceOf[List[_]]
  }

  "DefaultSlickApi" should {
    "check the assumption that the ApplicationLifecycle isn't null" in {
      val lifecycle = injector.instanceOf[ApplicationLifecycle]
      lifecycle must not beNull
    }
    "check the assumption that the ApplicationLifecycle is a singleton" in {
      val injector = self.injector
      val lifecycle1 = injector.instanceOf[ApplicationLifecycle]
      val lifecycle2 = injector.instanceOf[ApplicationLifecycle]
      lifecycle1 mustEqual lifecycle2
    }
    "check the assumption that ApplicationLifecycle is binded to a DefaultApplicationLifecycle" in {
      val lifecycle = injector.instanceOf[ApplicationLifecycle]
      lifecycle must beAnInstanceOf[DefaultApplicationLifecycle]
    }
    "check that no stop hook is registered in the ApplicationLifecycle at application start" in {
      val lifecycle = injector.instanceOf[ApplicationLifecycle].asInstanceOf[DefaultApplicationLifecycle]

      hooks(lifecycle) must be empty
    }
    "check that a stop hook is registered in the ApplicationLifecycle when a database is created" in {
      val injector = self.injector
      val lifecycle = injector.instanceOf[ApplicationLifecycle].asInstanceOf[DefaultApplicationLifecycle]
      val api = injector.instanceOf[SlickApi]
      val defaultDb = api.dbConfig[BasicProfile](DbName("default"))

      hooks(lifecycle) must have size(1)
    }
  }
}