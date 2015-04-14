package controllers

import scala.concurrent.Future

import models.Cat
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.toJson
import play.api.mvc.Action
import play.api.mvc.Controller
import slick.profile.RelationalProfile
import tables.CatTable

class Application extends Controller with CatTable {

  protected val (driver, db) = {
    val dbConfig = DatabaseConfigProvider.get[RelationalProfile](Play.current)
    (dbConfig.driver, dbConfig.db)
  }

  import driver.api._

  //create an instance of the table
  val Cats = TableQuery[Cats] //see a way to architect your app in the computers-database-slick sample

  def index = Action.async { implicit rs =>
    db.run(Cats.result).map { cats =>
      Ok(toJson(cats))
    }
  }

  def insert = Action.async(parse.json) { implicit request =>
    request.body.validate[Cat].map { cat =>
      db.run(Cats += cat).map(_ => Ok(toJson(cat)))
    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }
}
