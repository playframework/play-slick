package test

import play.api.test.FakeRequest
import play.api.test.PlaySpecification
import play.api.test.WithApplication
import play.api.libs.json.Json
import play.api.test.FakeHeaders

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpecification {

  "Application" should {
    "send 404 on a bad request" in new WithApplication {
      val result = route(FakeRequest(GET, "/boum")).get
      status(result) mustEqual NOT_FOUND
    }

    "render the index page" in new WithApplication {

      val kitty = Json.obj("name" -> "Scarlett", "color" -> "Black & White")
      val postRequest = FakeRequest(
                  method = "POST",
                  uri = "/insert",
                  headers = FakeHeaders(
                    Seq("Content-type"-> "application/json")
                  ),
                  body =  kitty
                )
      val result = route(postRequest).get
      status(result) mustEqual OK

      val home = route(FakeRequest(GET, "/")).get

      status(home) mustEqual OK
      contentType(home) must beSome.which(_ == "application/json")
      contentAsString(home) must contain ("Scarlett")
    }
  }
}
