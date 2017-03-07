/*
 * Copyright (C) 2011-2015 Typesafe Inc. <http://www.typesafe.com>
 */
package scalaguide.slick
package global

import scala.concurrent.Future

import javax.inject.Inject
import play.api.Application
import play.api.mvc._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import slick.driver.JdbcProfile

import UsersSchema._

class Application1 @Inject()(application: Application) extends Controller {
  //#global-lookup-database-config
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](application)
  //#global-lookup-database-config

  //#driver-import
  import dbConfig.driver.api._
  //#driver-import

  //#action-with-db
  def index(name: String) = Action.async { implicit request =>
    val resultingUsers: Future[Seq[User]] = dbConfig.db.run(Users.filter(_.name === name).result)
    resultingUsers.map(users => Ok(views.html.index(users)))
  }
  //#action-with-db
}

class Application2 @Inject()(application: Application) extends Controller {
  //#named-global-lookup-database-config
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("<db-name>")(application)
  //#named-global-lookup-database-config
}
