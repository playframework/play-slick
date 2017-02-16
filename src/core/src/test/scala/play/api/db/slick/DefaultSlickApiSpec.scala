package play.api.db.slick

import java.util.concurrent.ConcurrentLinkedDeque

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.inject.ApplicationLifecycle
import play.api.inject.DefaultApplicationLifecycle
import play.api.inject.guice.GuiceApplicationBuilder
import slick.basic.BasicProfile

class DefaultSlickApiSpec extends Specification with Mockito { self =>

  sequential

  // A new injector should be created to ensure each test is independent of each other
  def injector = GuiceApplicationBuilder(configuration = TestData.configuration).injector()

  def hooks(lifecycle: DefaultApplicationLifecycle): Array[_] = {
    val hooksField = lifecycle.getClass.getDeclaredField("hooks")
    hooksField.setAccessible(true)
    hooksField.get(lifecycle).asInstanceOf[ConcurrentLinkedDeque[_]].toArray
  }

  "DefaultSlickApi" should {
    "check the assumption that the ApplicationLifecycle isn't null" in {
      val lifecycle = injector.instanceOf[ApplicationLifecycle]
      lifecycle must not(beNull)
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

      // Play register its own stop hooks. This is why we won't have an empty array here.
      // For example, DBModule registers a hook to close the database pool.
      hooks(lifecycle) must have size (1)
    }
    "check that a stop hook is registered in the ApplicationLifecycle when a database is created" in {
      val injector = self.injector
      val lifecycle = injector.instanceOf[ApplicationLifecycle].asInstanceOf[DefaultApplicationLifecycle]
      val api = injector.instanceOf[SlickApi]
      api.dbConfig[BasicProfile](DbName("default"))

      hooks(lifecycle) must have size (2)
    }
  }
}