package test

import org.specs2.mutable._

import play.api.db.slick.DB
import play.api.db.slick.driver.simple._
import play.api.test._
import play.api.test.Helpers._
import models._

/**
  * test the kitty cat database
  */
class DBSpec extends Specification {

  "DB" should {
    "work as expected" in new WithApplication {
      val dao = new DAO(DB.driver)

      import dao._ //import all our database Tables
      import dao.profile.simple._ //import specific database methods
      DB.withSession{ implicit s:Session =>
        val testKitties = Seq(
          Cat("kit", "black"),
          Cat("garfield", "orange"),
          Cat("creme puff", "grey")
        )
        Cats.insertAll( testKitties: _*)
        Query(Cats).list must equalTo(testKitties)
      }
    }

    "select the correct testing db settings by default" in new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      play.api.db.slick.DB.withSession{ implicit s:Session =>
        s.conn.getMetaData.getURL must startWith("jdbc:h2:mem:play-test")
      }
    }

    "use the correct db settings when specified" in new WithApplication {
      play.api.db.slick.DB("specific").withSession{ implicit s:Session =>
        s.conn.getMetaData.getURL must equalTo("jdbc:h2:mem:veryspecialindeed")
      }
    }

    "use the default db settings when no other possible options are available" in new WithApplication {
      play.api.db.slick.DB.withSession{ implicit s:Session =>
        s.conn.getMetaData.getURL must equalTo("jdbc:h2:mem:play")
      }
    }
  }

}
