package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import dao.CatDAO
import models.Cat
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.text
import play.api.mvc.Action
import play.api.mvc.Controller

object Application extends Controller {
  // Mind that this member *HAS* to be a method. Turning it into a field (or a lazy field) will make
  // some of the tests fail. To declared it as a field, you will need to turn this controller into a
  // class.
  def dao = new CatDAO

  def index = Action.async {
    dao.all().map(c => Ok(views.html.index(c)))
  }

  private val catForm = Form(
    mapping(
      "name" -> text(),
      "color" -> text()
    )(Cat.apply)(Cat.unapply)
  )

  def insert = Action.async { implicit request =>
    val cat: Cat = catForm.bindFromRequest.get
    dao.insert(cat).map(_ => Redirect(routes.Application.index))
  }
}