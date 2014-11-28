package test

import org.specs2.mutable._

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.test._
import play.api.test.Helpers._
import models._

/**
 * test the kitty cat database
 */
class DBSpec extends Specification {

  "DB" should {
    "work as expected" in new WithApplication {

      //create an instance of the table
      val Cats = TableQuery[CatsTable] //see a way to architect your app in the computers-database-slick sample

      DB.withSession { implicit s: Session =>
        val testKitties = Seq(
          Cat("kit", "black"),
          Cat("garfield", "orange"),
          Cat("creme puff", "grey"))
        Cats.insertAll(testKitties: _*)
        Cats.list must equalTo(testKitties)
      }
    }

    "select the correct testing db settings by default" in new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      DB.withSession { implicit s: Session =>
        s.conn.getMetaData.getURL must startWith("jdbc:h2:mem:play-test")
      }
    }

    "use the default db settings when no other possible options are available" in new WithApplication {
      DB.withSession { implicit s: Session =>
        s.conn.getMetaData.getURL must equalTo("jdbc:h2:mem:play")
      }
    }
  }

}
