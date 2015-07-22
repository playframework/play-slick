package play.api.db.slick.evolutions

import org.specs2.mutable.Specification
import play.api.db.DBApi
import play.api.db.slick.TestData
import play.api.inject.guice.GuiceApplicationBuilder

class DBApiAdapterSpec extends Specification {

  "DBApiAdapter" >> {
    val appBuilder = new GuiceApplicationBuilder(configuration = TestData.configuration)
    val injector = appBuilder.injector()

    val api = injector.instanceOf[DBApi]
    val db = api.database("somedb")

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
  }
}
