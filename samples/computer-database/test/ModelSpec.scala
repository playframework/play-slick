package test

import play.api.Application

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import org.specs2.mutable.Specification

import dao.CompaniesDAO
import dao.ComputersDAO
import play.api.test.WithApplication

class ModelSpec extends Specification {

  import models._

  // -- Date helpers

  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str

  // --

  "Computer model" should {

    def companiesDao(implicit app: Application) = {
      val app2CompaniesDAO = Application.instanceCache[CompaniesDAO]
      app2CompaniesDAO(app)
    }

    def computersDao(implicit app: Application) = {
      val app2ComputersDAO = Application.instanceCache[ComputersDAO]
      app2ComputersDAO(app)
    }

    "be retrieved by id" in new WithApplication {
      val macintosh = Await.result(computersDao.findById(21), Duration.Inf).get
      macintosh.name must equalTo("Macintosh")
      macintosh.introduced must beSome.which(dateIs(_, "1984-01-24"))
    }

    "be listed along its companies" in new WithApplication {
      val computers = Await.result(computersDao.list(), Duration.Inf)
      computers.total must equalTo(574)
      computers.items must have length (10)
    }

    "be updated if needed" in new WithApplication {
      Await.result(computersDao.update(21, Computer(name = "The Macintosh", introduced = None, discontinued = None, companyId = Some(1))), Duration.Inf)

      val macintosh = Await.result(computersDao.findById(21), Duration.Inf).get
      macintosh.name must equalTo("The Macintosh")
      macintosh.introduced must beNone

    }

  }

}