package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.db.slick.DB
import models._

//stable imports to use play.api.Play.current outside of objects:
import models.current.dao._
import models.current.dao.profile.simple._

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
