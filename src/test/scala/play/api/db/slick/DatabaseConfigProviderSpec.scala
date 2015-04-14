package play.api.db.slick

import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.Mode
import play.api.db.evolutions.EvolutionsModule
import play.api.inject.guice.GuiceApplicationBuilder
import slick.profile.BasicProfile
import org.specs2.mutable.After
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DatabaseConfigProviderSpec extends Specification {

  implicit val app = {
    val testConfiguration: Configuration = Configuration.from(
      Map(
        "slick.dbs.somedb.driver" -> "slick.driver.H2Driver$",
        "slick.dbs.somedb.db.driver" -> "org.h2.Driver",

        "slick.dbs.default.driver" -> "slick.driver.MySQLDriver$",
        "slick.dbs.default.db.driver" -> "com.mysql.jdbc.Driver",

        "slick.dbs.jdbc-driver-not-recognized.driver" -> "slick.driver.MySQLDriver$",
        "slick.dbs.jdbc-driver-not-recognized.db.driver" -> "play.api.db.slick.SomeDummyDriver"))

    new GuiceApplicationBuilder()
      .configure(testConfiguration)
      .in(this.getClass.getClassLoader)
      .in(Mode.Dev)
      .disable[EvolutionsModule]
      .build()
  }

  "DatabaseConfigProvider" should {
    "return the configured slick driver for the given database" in {
      val config = DatabaseConfigProvider.get[BasicProfile]("somedb")
      val driver = config.driver
      driver must equalTo(_root_.slick.driver.H2Driver)
    }

    "return the configured driver for the default database when db name is not specified" in {
      val config = DatabaseConfigProvider.get[BasicProfile]
      val driver = config.driver
      driver must equalTo(_root_.slick.driver.MySQLDriver)
    }

    "throw when accessing the db if an invalid jdbc driver is configured" in {
      val config = DatabaseConfigProvider.get[BasicProfile]("jdbc-driver-not-recognized")
      config.db must throwA[Throwable]("""driverClassName specified class 'play.api.db.slick.SomeDummyDriver' could not be loaded *""")
    }
  }
}
