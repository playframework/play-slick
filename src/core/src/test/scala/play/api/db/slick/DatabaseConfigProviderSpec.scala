package play.api.db.slick

import org.specs2.mutable.Specification

import play.api.Application
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import slick.basic.BasicProfile

class DatabaseConfigProviderSpec extends Specification {

  def withApp[T](block: Application => T): T = {
    val app = new GuiceApplicationBuilder()
      .configure(TestData.configuration)
      .in(Mode.Test)
      .build()

    try {
      block(app)
    } finally {
      app.stop()
    }
  }
  "DatabaseConfigProvider" should {
    "return the configured slick profile for the given database" in withApp { implicit app =>
      val config  = DatabaseConfigProvider.get[BasicProfile]("somedb")
      val profile = config.profile
      profile must equalTo(_root_.slick.jdbc.H2Profile)
    }

    "return the configured profile for the default database when db name is not specified" in withApp { implicit app =>
      val config  = DatabaseConfigProvider.get[BasicProfile]
      val profile = config.profile
      profile must equalTo(_root_.slick.jdbc.MySQLProfile)
    }

    "throw when accessing the db if an invalid jdbc driver is configured" in withApp { implicit app =>
      val config = DatabaseConfigProvider.get[BasicProfile]("jdbc-driver-not-recognized")
      config.db must throwA[Throwable]("""Failed to load driver class play.api.db.slick.SomeDummyDriver""")
    }
  }
}
