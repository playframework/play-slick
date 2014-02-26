package test

import java.util.concurrent.TimeUnit

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class IntegrationSpec extends Specification {

  "Application" should {

    "return all records, which were enumerated in chunks" in {
      running(TestServer(3333), HTMLUNIT) { browser =>

        // verify that comet handler received records in chunks
        browser.goTo("http://localhost:3333/")
        browser.await().atMost(2, TimeUnit.SECONDS).until("#items-list ul li").isPresent()
        browser.pageSource must contain("""{"id":1,"name":"Alpha"}""")
        browser.pageSource must contain("""{"id":2,"name":"Beta"}""")
        browser.pageSource must contain("""{"id":3,"name":"Gamma"}""")
        browser.pageSource must contain("""{"id":4,"name":"Delta"}""")
        browser.pageSource must contain("""{"id":5,"name":"Epsilon"}""")
      }
    }

  }

}