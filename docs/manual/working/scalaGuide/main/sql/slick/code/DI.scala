/*
 * Copyright (C) 2011-2015 Typesafe Inc. <http://www.typesafe.com>
 */
package scalaguide.slick
package di

import javax.inject.Inject

import scala.concurrent.Future

import play.api.mvc._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import slick.profile.RelationalProfile

import UsersSchema._

//#di-database-config
class Application @Inject()(dbConfigProvider: DatabaseConfigProvider) extends Controller {
  val dbConfig = dbConfigProvider.get[RelationalProfile]
  //#di-database-config

  import dbConfig.driver.api._

  def index(name: String) = Action.async { implicit request =>
    val resultingUsers: Future[Seq[User]] = dbConfig.db.run(Users.filter(_.name === name).result)
    resultingUsers.map(users => Ok(views.html.index(users)))
  }
}

import play.db.NamedDatabase
//#named-di-database-config
class Application2 @Inject()(@NamedDatabase("<db-name>") dbConfigProvider: DatabaseConfigProvider) extends Controller {
  //#named-di-database-config
}