package play.api.db.slick.test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config
import play.api.PlayException
import scala.slick.driver.ExtendedDriver

object NotAnExtendedDriver{
}
object Enclosing{
  object SomeExtendedDriver extends ExtendedDriver{
  }
}
class ConfigSpec extends Specification {

  //abstract class SomeDummyDriver extends java.sql.Driver{}

  class BadSlickDriver{}


  def testConfiguration = {
    Map(
      "db.somedb.driver" -> "org.h2.Driver",

      "db.default.driver" -> "com.mysql.jdbc.Driver",

      "db.custom-unknown.driver" -> "play.api.db.slick.test.SomeDummyDriver",
      "db.custom-unknown.slickdriver" -> "scala.slick.driver.PostgresDriver",

      "db.custom-known.driver" -> "org.h2.Driver",
      "db.custom-known.slickdriver" -> "scala.slick.driver.PostgresDriver",

      "db.custom-nested.driver" -> "org.h2.Driver",
      "db.custom-nested.slickdriver" -> "play.api.db.slick.test.Enclosing$SomeExtendedDriver",

      "db.badDriver.driver"-> "play.api.db.slick.test.SomeDummyDriver",

      "db.badClass.driver"-> "play.api.db.slick.test.SomeDummyDriver",
      "db.badClass.slickdriver" -> classOf[BadSlickDriver].getName,

      "db.badDriverClass.driver"-> "play.api.db.slick.test.SomeDummyDriver",
      "db.badDriverClass.slickdriver" -> "play.api.db.slick.test.NotAnExtendedDriver",

      "evolutionplugin" -> "disabled")
  }

  def fakeApplication = FakeApplication(
    withoutPlugins = Seq("play.api.db.BoneCPPlugin"),
    additionalConfiguration = testConfiguration)

  "Config.driver" should {
    "return the driver for the given database" in {
      running(fakeApplication) {
        val driver = Config.driver("somedb")(play.api.Play.current)
        driver must equalTo(scala.slick.driver.H2Driver)
      }
    }

    "return the driver for the default database when db name is not specified" in {
      running(fakeApplication) {
        val driver = Config.driver()(play.api.Play.current)
        driver must equalTo(scala.slick.driver.MySQLDriver)
      }
    }

    "return the arbitrary driver if specified (for unknown JDBC driver)" in {
      running(fakeApplication) {
        val driver = Config.driver("custom-unknown")(play.api.Play.current)
        driver must equalTo(scala.slick.driver.PostgresDriver)
      }
    }

    "return the arbitrary driver if specified (for known JDBC driver)" in {
      running(fakeApplication) {
        val driver = Config.driver("custom-known")(play.api.Play.current)
        driver must equalTo(scala.slick.driver.PostgresDriver)
      }
    }

    "return the arbitrary driver if specified (for known JDBC driver)" in {
      running(fakeApplication) {
        val driver = Config.driver("custom-nested")(play.api.Play.current)
        driver must equalTo(Enclosing.SomeExtendedDriver)
      }
    }

    "give a good advice if driver is not supported" in {
      running(fakeApplication) {
        Config.driver("badDriver")(play.api.Play.current) must throwA[Throwable]("""If you have a Slick driver for this database, you can put its class name to db\.badDriver\.slickdriver\.""")
      }
    }

    "give a good error message if a class is passed instead of object" in {
      running(fakeApplication) {
        Config.driver("badClass")(play.api.Play.current) must throwA[Throwable]("""The class play\.api\.db\.slick\.test\.ConfigSpec\$BadSlickDriver is not an object. Use 'object' keyword, please\.""")
      }
    }

    "give a good error message if the driver object is not an ExtendedDriver" in {
      running(fakeApplication) {
        Config.driver("badDriverClass")(play.api.Play.current) must throwA[PlayException.ExceptionSource]("""The class play\.api\.db\.slick\.test\.NotAnExtendedDriver is not a scala\.slick\.driver\.ExtendedDriver\.""")
      }
    }

  }
}
