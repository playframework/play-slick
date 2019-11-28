package play.api.db.slick.evolutions

import org.specs2.mutable.Specification
import play.api.db.DBApi
import play.api.db.slick.TestData
import play.api.inject.guice.GuiceApplicationBuilder

class DBApiAdapterSpec extends Specification {

  "DBApiAdapter" >> {
    val appBuilder = GuiceApplicationBuilder(configuration = TestData.configuration)
    val injector   = appBuilder.injector()

    val api    = injector.instanceOf[DBApi]
    val dbName = "somedb"
    val db     = api.database(dbName)

    "getConnection" should {
      "respect autocommit parameter" in {
        db.getConnection(autocommit = false).getAutoCommit must_== false
        db.getConnection(autocommit = true).getAutoCommit must_== true
      }

      "default autocommit to true" in {
        db.getConnection().getAutoCommit must_== true
      }
    }

    "withConnection" should {
      "respect autocommit parameter" in {
        var called = false
        db.withConnection(autocommit = false) { conn =>
          conn.getAutoCommit must_== false
          called = true
        }
        called must_== true

        called = false
        db.withConnection(autocommit = true) { conn =>
          conn.getAutoCommit must_== true
          called = true
        }
        called must_== true
      }

      "default autocommit to true" in {
        var called = false
        db.withConnection { conn =>
          conn.getAutoCommit must_== true
          called = true
        }
        called must_== true
      }
    }

    "url" should {
      "return the value set in the config for the jdbc url" in {
        db.url must_== TestData.configuration.get[String](s"slick.dbs.$dbName.db.url")
      }
      "return the value set in the config for the datasource url" in {
        val h2DatasourceDb = api.database("h2datasource")
        h2DatasourceDb.url must_== TestData.configuration.get[String](s"slick.dbs.h2datasource.db.properties.url")
      }
    }
  }
}
