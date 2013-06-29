package controllers

import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.db.slick.session._
import play.api.db.slick.mvc._
import models._

//stable imports to use play.api.Play.current outside of objects:
import models.current.dao._
import models.current.dao.profile.simple._

object Application extends Controller with DBController{
  def index = DBAction { implicit rs =>
    Ok(views.html.index(Query(Cats).list))
  }

  val catForm = Form(
    mapping(
      "name" -> text(),
      "color" -> text()
    )(Cat.apply)(Cat.unapply)
  )
  
  def insert = DBAction { implicit rs =>
    val cat = catForm.bindFromRequest.get
    Cats.insert(cat)
    Redirect(routes.Application.index)
  }
  
}
