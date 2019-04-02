package controllers

import dao.CatDAO
import dao.DogDAO
import javax.inject.Inject

import models.Cat
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.text
import play.api.mvc.{ AbstractController, ControllerComponents }
import models.Dog

import scala.concurrent.ExecutionContext

class Application @Inject() (
  catDao: CatDAO,
  dogDao: DogDAO,
  controllerComponents: ControllerComponents)(implicit executionContext: ExecutionContext) extends AbstractController(controllerComponents) {

  def index = Action.async {
    catDao.all().zip(dogDao.all()).map { case (cats, dogs) => Ok(views.html.index(cats, dogs)) }
  }

  val catForm = Form(
    mapping(
      "name" -> text(),
      "color" -> text())(Cat.apply)(Cat.unapply))

  val dogForm = Form(
    mapping(
      "name" -> text(),
      "color" -> text())(Dog.apply)(Dog.unapply))

  def insertCat = Action.async { implicit request =>
    val cat: Cat = catForm.bindFromRequest.get
    catDao.insert(cat).map(_ => Redirect(routes.Application.index))
  }

  def insertDog = Action.async { implicit request =>
    val dog: Dog = dogForm.bindFromRequest.get
    dogDao.insert(dog).map(_ => Redirect(routes.Application.index))
  }
}
