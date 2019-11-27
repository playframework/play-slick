package play.api.db.slick

import java.util.concurrent.ConcurrentLinkedDeque

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.inject.Injector
import play.api.inject.ApplicationLifecycle
import play.api.inject.DefaultApplicationLifecycle
import play.api.inject.guice.GuiceApplicationBuilder
import slick.basic.BasicProfile

class DefaultSlickApiSpec extends Specification with Mockito { self =>

  sequential

  // A new injector should be created to ensure each test is independent of each other
  def injector: Injector = GuiceApplicationBuilder(configuration = TestData.configuration).injector()

  def hooks(lifecycle: DefaultApplicationLifecycle): Array[_] = {
    val hooksField = lifecycle.getClass.getDeclaredField("hooks")
    hooksField.setAccessible(true)
    hooksField.get(lifecycle).asInstanceOf[ConcurrentLinkedDeque[_]].toArray()
  }

  "DefaultSlickApi" should {

    "check the assumption that the ApplicationLifecycle isn't null" in {
      val lifecycle = injector.instanceOf[ApplicationLifecycle]
      lifecycle must not(beNull)
    }

    "check the assumption that the ApplicationLifecycle is a singleton" in {
      val injector   = self.injector
      val lifecycle1 = injector.instanceOf[ApplicationLifecycle]
      val lifecycle2 = injector.instanceOf[ApplicationLifecycle]
      lifecycle1 mustEqual lifecycle2
    }

    "check the assumption that ApplicationLifecycle is binded to a DefaultApplicationLifecycle" in {
      val lifecycle = injector.instanceOf[ApplicationLifecycle]
      lifecycle must beAnInstanceOf[DefaultApplicationLifecycle]
    }

    "check that a stop hook is registered by SlickApi in the ApplicationLifecycle when a database is created" in {
      val injector  = self.injector
      val lifecycle = injector.instanceOf[ApplicationLifecycle].asInstanceOf[DefaultApplicationLifecycle]

      // collect the num of existing hooks added by Play
      val originalNumOfHooks = hooks(lifecycle).length

      val api = injector.instanceOf[SlickApi]
      api.dbConfig[BasicProfile](DbName("default"))

      // confirm that SlickApi is adding its own hook
      hooks(lifecycle) must have size (originalNumOfHooks + 1)
    }
  }
}
