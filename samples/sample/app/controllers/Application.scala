package controllers

import models._
import play.api._
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller{
  //create an instance of the table
  val Cats = TableQuery[CatsTable] //see a way to architect your app in the computers-database-slick sample

  def index = DBAction { implicit rs =>
    Ok(views.html.index(Cats.list))
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
