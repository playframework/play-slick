package controllers

import models._
import play.api._
import play.api.db.slick._
import play.api.db.slick.driver.simple._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller{

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