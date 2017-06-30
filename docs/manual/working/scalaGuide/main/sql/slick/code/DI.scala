/*
 * Copyright (C) 2011-2015 Typesafe Inc. <http://www.typesafe.com>
 */
package scalaguide.slick
package di

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }
import play.api.mvc._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }

import slick.jdbc.JdbcProfile
import UsersSchema._

//#di-database-config
class Application @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {
  //#di-database-config

  import profile.api._

  def index(name: String) = Action.async { implicit request =>
    val resultingUsers: Future[Seq[User]] = db.run(Users.filter(_.name === name).result)
    resultingUsers.map(users => Ok(views.html.index(users)))
  }
}

import play.db.NamedDatabase
//#named-di-database-config
class Application2 @Inject() (
    @NamedDatabase("<db-name>") protected val dbConfigProvider: DatabaseConfigProvider,
    cc: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] {
  //#named-di-database-config
}
