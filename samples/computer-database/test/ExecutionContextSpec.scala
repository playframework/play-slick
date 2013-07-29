package play.api.db.slick.test

import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.DBAction
import play.api.mvc._


object Application extends Controller { 
  def threadTest = DBAction { 
     Ok(Thread.currentThread().getName)
  }
}


class IntegrationSpec extends Specification {
 
 "use silck thread pool" in {
      running(FakeApplication()) {
        val result = Application.threadTest (FakeRequest())
        contentAsString(result) must startWith("application-akka.actor.slick-context")
     }
  }  
}