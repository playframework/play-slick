/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package scalaguide.slick

import play.api.mvc._
import play.api.test._

import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._

object ScalaSlickDriversSpec extends PlaySpecification {

  import ScalaSlickSchema._

  "DB" should {

    "support named data sources" in {
      val app = FakeApplication(additionalConfiguration = inMemoryDatabase("other") ++
        inMemoryDatabase())

      new WithApplication(app) {
        DB("other").withSession { implicit session =>
          Users.ddl.create
        }

        //#db-other
        DB("other").withSession{ implicit session =>
          Users.insert(User("fredrik", "ekholdt"))
        }
        //#db-other

        DB("other").withSession { implicit session =>
          Users.list must_== List(User("fredrik", "ekholdt"))
        }
      }
    }
  }

}
