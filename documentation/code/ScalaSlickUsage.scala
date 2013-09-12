package scalaguide.slick.scalaslickusage

import org.specs2.mutable._

import play.api.db.slick.DB
//#import
import play.api.db.slick.Config.driver.simple._
//#import

import play.api.test._
import play.api.test.Helpers._

case class Cat(name: String, color: String)

object Cats extends Table[Cat]("CAT") {

  def name = column[String]("name", O.PrimaryKey)
  def color = column[String]("color", O.NotNull)

  def * = name ~ color <> (Cat.apply _, Cat.unapply _)
}

/**
  * test the kitty cat database
  */
class DBSpec extends PlaySpecification { 

  "DB" should {
    "work as expected" in new WithApplication {
      //#insert
      DB.withSession{ implicit s:Session =>
        val testKitties = Seq(
          Cat("kit", "black"),
          Cat("garfield", "orange"),
          Cat("creme puff", "grey")
        )
        Cats.insertAll( testKitties: _*)
      }
      //#insert
      DB.withSession{ implicit s:Session =>
        Query(Cats).list must equalTo(testKitties)
      }
    }

    "select the correct testing db settings by default" in new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      DB.withSession{ implicit s:Session =>
        s.conn.getMetaData.getURL must startWith("jdbc:h2:mem:play-test")
      }
    }

    "use the default db settings when no other possible options are available" in new WithApplication {
      DB.withSession{ implicit s:Session =>
        s.conn.getMetaData.getURL must equalTo("jdbc:h2:mem:play")
      }
    }
  }

}
