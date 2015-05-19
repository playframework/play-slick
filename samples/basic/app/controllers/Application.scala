package controllers

import models.Cat
import play.api.Play
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.text
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller
import slick.driver.JdbcProfile
import tables.CatTable

class Application extends Controller with CatTable with HasDatabaseConfig[JdbcProfile]{
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import driver.api._

  //create an instance of the table
  val Cats = TableQuery[Cats] //see a way to architect your app in the computers-database sample

  def index = Action.async {
    db.run(Cats.result).map(res => Ok(views.html.index(res.toList)))
  }

  val catForm = Form(
    mapping(
      "name" -> text(),
      "color" -> text()
    )(Cat.apply)(Cat.unapply)
  )

  def insert = Action.async { implicit rs =>
    val cat = catForm.bindFromRequest.get
    db.run(Cats += cat).map(_ => Redirect(routes.Application.index))
  }
}