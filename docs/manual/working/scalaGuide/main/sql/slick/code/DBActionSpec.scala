/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package scalaguide.slick

import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import play.api.mvc._
import play.api.test._

object DBActionSpec extends PlaySpecification with Controller {

  case class User(name: String, surname: String)

  class UsersTable(tag: Tag) extends Table[User](tag, "USER") {
    def name = column[String]("name", O.PrimaryKey)
    def surname = column[String]("surname", O.NotNull)
    def * = (name, surname) <> (User.tupled, User.unapply _)
  }

  val Users = TableQuery[UsersTable]

  val app = FakeApplication(additionalConfiguration = inMemoryDatabase())

  DB(app).withSession { implicit session =>
    Users.ddl.create
    Users.insertAll(User("alice", "smith"))
  }

  "DBAction" should {

    "provide implicit slick session" in {
      //#implicit-session
      def index(name: String) = DBAction { implicit rs =>
        val users = Users.filter(_.name === name)
        Ok(views.html.index(users.list))
      }
      //#implicit-session

      running(app) {
        contentAsString(index("alice")(FakeRequest())).trim must_== "alice smith"
      }
    }

  }

}
