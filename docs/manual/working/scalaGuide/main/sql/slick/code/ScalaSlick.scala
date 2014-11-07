/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package scalaguide.slick

import play.api.mvc._
import play.api.test._

//#driver-import
import play.api.db.slick.Config.driver.simple._
//#driver-import

object ScalaSlickSpec extends PlaySpecification with Controller {

  import ScalaSlickSchema._

  "DBAction" should {
    //#dbaction-import
    import play.api.db.slick._
    //#dbaction-import

    "provide implicit slick session" in {
      //#dbaction-session
      def index(name: String) = DBAction { implicit rs =>
        val users = Users.filter(_.name === name)
        Ok(views.html.index(users.list))
      }
      //#dbaction-session

      val app = FakeApplication(additionalConfiguration = inMemoryDatabase())

      running(app) {
        DB(app).withSession { implicit session =>
          Users.ddl.create
          Users.insert(User("alice", "smith"))
        }
        contentAsString(index("alice")(FakeRequest())).trim must_== "alice smith"
      }
    }
  }

  "DB wrapper" should {
    //#db-import
    import play.api.db.slick.DB
    //#db-import

    "provide withSession" in {
      val app = FakeApplication(additionalConfiguration = inMemoryDatabase())

      running(app) {
        //#import-implicit-app
        // import implicit Application
        import play.api.Play.current
        //#import-implicit-app

        DB.withSession { implicit session =>
          Users.ddl.create
        }

        //#db-session
        DB.withSession { implicit session =>
          Users.insert(User("fredrik", "ekholdt"))
        }
        //#db-session

        DB.withSession { implicit session =>
          Users.list must_== List(User("fredrik", "ekholdt"))
        }
      }
    }

    "provide withTransaction" in {
      val app = FakeApplication(additionalConfiguration = inMemoryDatabase())

      running(app) {
        import play.api.Play.current

        DB.withSession { implicit session =>
          Users.ddl.create
          Users.insert(User("alice", "smith"))
        }

        val someFailure = true

        //#db-transaction
        DB.withTransaction{ implicit session =>
          Users.insert(User("fredrik", "ekholdt"))
          if (someFailure) {
            session.rollback
          }
        }
        //#db-transaction

        DB.withSession { implicit session =>
          Users.list must_== List(User("alice", "smith"))
        }
      }
    }
  }

}
