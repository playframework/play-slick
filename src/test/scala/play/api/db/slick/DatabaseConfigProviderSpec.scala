package play.api.db.slick

import org.specs2.mutable.Specification

import play.api.Mode
import play.api.db.evolutions.EvolutionsModule
import play.api.inject.guice.GuiceApplicationBuilder
import slick.profile.BasicProfile

class DatabaseConfigProviderSpec extends Specification {

  implicit val app = {

    new GuiceApplicationBuilder()
      .configure(TestData.configuration)
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
