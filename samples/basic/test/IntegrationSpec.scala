package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class IntegrationSpec extends Specification {

  "Application" should {

    "work from within a browser" in {
      val port = 3333
      running(TestServer(port), HTMLUNIT) { browser =>

        browser.goTo("http://localhost:" + port)

        browser.pageSource must contain("kitty cat")

      }
    }

  }

}
