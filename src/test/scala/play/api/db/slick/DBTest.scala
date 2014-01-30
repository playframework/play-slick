package play.api.db.slick.test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config

class DBSpec extends Specification {

  def testConfiguration = {
    Map(
      "db.default.url" -> "jdbc:hsqldb:mem:play",
      "db.default.user" -> "sa",
      "db.default.password" -> "",
      "db.somedb.url" -> "jdbc:h2:mem:play",
      "db.somedb.user" -> "sa",
      "db.somedb.password" -> "",
      "db.somedb.driver" -> "org.h2.Driver",
      "db.default.driver" -> "org.hsqldb.jdbcDriver",
      "evolutionplugin" -> "disabled")
  }

  def fakeApplication = FakeApplication(
    additionalConfiguration = testConfiguration)

  "DB.driver" should {
    "return the driver for the default database when db name is not specified" in {
      running(fakeApplication)
      {
        val driver = DB(play.api.Play.current).driver
        driver must equalTo(scala.slick.driver.HsqldbDriver)
      }
    }
    "return the driver for a specified database" in {
      running(fakeApplication) {
        val driver = DB("somedb")(play.api.Play.current).driver
        driver must equalTo(scala.slick.driver.H2Driver)
      }
    }
  }
}
