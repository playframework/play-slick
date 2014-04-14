package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication {

      val kitty = Json.obj("name" -> "Scarlett", "color" -> "Black & White")
      val postRequest = FakeRequest(
                  method = "POST",
                  uri = "/insert",
                  headers = FakeHeaders(
                    Seq("Content-type"->Seq("application/json"))
                  ),
                  body =  kitty
                )
      val Some(result) = route(postRequest)
      status(result) must equalTo(OK)

      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "application/json")
      contentAsString(home) must contain ("Scarlett")
    }
  }
}
