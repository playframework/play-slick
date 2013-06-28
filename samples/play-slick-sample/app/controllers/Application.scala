package controllers

import models._
import models.tables._
import play.api._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

object Application extends Controller {

  import play.api.Play.current

  def index = Action {
    DB.withSession{ implicit session =>
      Ok(views.html.index(Query(Cats).list))
    }
  }

  val catForm = Form(
    mapping(
      "name" -> text(),
      "color" -> text()
    )(Cat.apply)(Cat.unapply)
  )

  def insert = Action { implicit request =>
    val cat = catForm.bindFromRequest.get
    DB.withSession{ implicit session =>
      Cats.insert(cat)
    }

    Redirect(routes.Application.index)
  }
  
}