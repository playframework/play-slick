/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package scalaguide.slick
package global

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.mvc._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scalaguide.slick.UsersSchema._

class Application1 @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, cc: ControllerComponents)(
    implicit ec: ExecutionContext
) extends AbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] {

  //#driver-import
  import dbConfig.profile.api._
  //#driver-import

  //#action-with-db
  def index(name: String) = Action.async { implicit request =>
    val resultingUsers: Future[Seq[User]] = db.run(Users.filter(_.name === name).result)
    resultingUsers.map(users => Ok(views.html.index(users)))
  }
  //#action-with-db
}
