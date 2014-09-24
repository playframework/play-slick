package play.api.db.slick.test

import java.io.File
import org.specs2.mutable._
import play.api._
import play.api.db._
import play.api.db.slick._
import play.api.test._
import scala.slick.driver.JdbcDriver

object NotAnExtendedDriver{
}
object Enclosing{
  object SomeExtendedDriver extends JdbcDriver {
  }
}
class ConfigSpec extends Specification {

  //abstract class SomeDummyDriver extends java.sql.Driver{}

  class BadSlickDriver{}

  val testConfiguration = Configuration.from(
    Map(
      "db.somedb.driver" -> "org.h2.Driver",

      "db.default.driver" -> "com.mysql.jdbc.Driver",

      "db.custom-unknown.driver" -> "play.api.db.slick.test.SomeDummyDriver",
      "db.custom-unknown.slickdriver" -> "scala.slick.driver.PostgresDriver",

      "db.custom-known.driver" -> "org.h2.Driver",
      "db.custom-known.slickdriver" -> "scala.slick.driver.PostgresDriver",

      "db.custom-nested.driver" -> "org.h2.Driver",
      "db.custom-nested.slickdriver" -> "play.api.db.slick.test.Enclosing$SomeExtendedDriver",

      "db.hsqldb-jdbc-driver.driver"-> "org.hsqldb.jdbcDriver",
      "db.hsqldb-alternative-jdbc-driver.driver"-> "org.hsqldb.jdbc.JDBCDriver",

      "db.jdbc-driver-not-recognized.driver"-> "play.api.db.slick.test.SomeDummyDriver",

      "db.class-instead-of-object.driver"-> "play.api.db.slick.test.SomeDummyDriver",
      "db.class-instead-of-object.slickdriver" -> classOf[BadSlickDriver].getName,

      "db.bad-object-type.driver"-> "play.api.db.slick.test.SomeDummyDriver",
      "db.bad-object-type.slickdriver" -> "play.api.db.slick.test.NotAnExtendedDriver"
    )
  )

  val config = new DefaultSlickConfig(
    testConfiguration,
    Environment(new File("."), this.getClass.getClassLoader, Mode.Dev),
    new DefaultDBApi(testConfiguration)
  )

  "SlickConfig.driver" should {
    "return the driver for the given database" in {
      val driver = config.driver("somedb")
      driver must equalTo(scala.slick.driver.H2Driver)
    }

    "return the driver for the default database when db name is not specified" in {
      val driver = config.driver()
      driver must equalTo(scala.slick.driver.MySQLDriver)
    }

    "return the arbitrary driver if specified (for unknown JDBC driver)" in {
      val driver = config.driver("custom-unknown")
      driver must equalTo(scala.slick.driver.PostgresDriver)
    }

    "return the arbitrary driver if specified (for known JDBC driver)" in {
      val driver = config.driver("custom-known")
      driver must equalTo(scala.slick.driver.PostgresDriver)
    }

    "return the arbitrary driver if specified (for known JDBC driver)" in {
      val driver = config.driver("custom-nested")
      driver must equalTo(Enclosing.SomeExtendedDriver)
    }

    "return HSQLDB driver for both drivers names" in {
      val hsqldbDriver = config.driver("hsqldb-jdbc-driver")
      val altHsqldbDriver = config.driver("hsqldb-alternative-jdbc-driver")

      hsqldbDriver must equalTo(scala.slick.driver.HsqldbDriver)
      altHsqldbDriver must equalTo(scala.slick.driver.HsqldbDriver)
    }

    "give a good advice if driver is not supported" in {
      config.driver("jdbc-driver-not-recognized") must throwA[Throwable]("""If you have a Slick driver for this database, you can put its class name to db\.jdbc-driver-not-recognized\.slickdriver\.""")
    }

    "give a good error message if a class is passed instead of object" in {
      config.driver("class-instead-of-object") must throwA[Throwable]("""The class play\.api\.db\.slick\.test\.ConfigSpec\$BadSlickDriver is not an object. Use 'object' keyword, please\.""")
    }

    "give a good error message if the driver object is not an JdbcDriver" in {
      config.driver("bad-object-type") must throwA[PlayException.ExceptionSource]("""The class play\.api\.db\.slick\.test\.NotAnExtendedDriver is not a scala\.slick\.driver\.JdbcDriver\.""")
    }

  }
}
