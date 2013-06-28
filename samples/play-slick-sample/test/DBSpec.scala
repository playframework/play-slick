package test

import org.specs2.mutable._

import play.api.db.slick.DB
import play.api.test._
import play.api.test.Helpers._
import models._
import models.tables._

/**
  * test the kitty cat database
  */
class DBSpec extends Specification { 

  "DB" should {
    "work as expected" in new WithApplication {

      import play.api.db.slick.Config.driver.simple._

      DB.withSession{ implicit session =>
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
      play.api.db.slick.DB.withSession{ implicit session =>
        session.conn.getMetaData.getURL must startWith("jdbc:h2:mem:play-test")
      }
    }

    "use the default db settings when no other possible options are available" in new WithApplication {
      play.api.db.slick.DB.withSession{ implicit session =>
        session.conn.getMetaData.getURL must equalTo("jdbc:h2:mem:play")
      }
    }
  }

}
