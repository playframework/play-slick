package scalaguide.slick.scalaslickdrivers

import org.specs2.mutable._

import play.api.db.slick.DB
import play.api.db.slick.Profile
import slick.driver.ExtendedProfile
import play.api.db.slick.Config.driver.simple._
import play.api.test._
import play.api.test.Helpers._


case class Cat(name: String, color: String)

/**
  * This Cat component contains the database representation of your
  * furry friends
  *
  * This pattern is called the cake pattern (I think it is because
  * it tastes good :P),
  *
  * Do not worry about the scary and yummy name, it is easily copyable!
  *
  * Just follow the steps
  * for each Table you want to have:
  *  1. the play.api.db.slick.Profile "self-type" (see below for an example)
  *  2. the import profile.simple._
  *
  * The reason you want to use the cake pattern here is because
  * we imagine we have multiple different databases for production
  * and tests
  */

//#component
trait CatComponent { this: Profile => //<- step 1: you must add this "self-type"
  import profile.simple._ //<- step 2: then import the correct Table, ... from the profile

  object Cats extends Table[Cat]("CAT") {

    def name = column[String]("name", O.PrimaryKey)
    def color = column[String]("color", O.NotNull)

    def * = name ~ color <> (Cat.apply _, Cat.unapply _)
  }
}
//#component

//#dao
class DAO(override val profile: ExtendedProfile) extends CatComponent with Profile
//#dao

//#current
object current {
  val dao = new DAO(DB(play.api.Play.current).driver)
}
//#current

/**
  * test the kitty cat database
  */
class DBSpec extends PlaySpecification {

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
