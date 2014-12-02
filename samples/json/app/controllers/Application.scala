package controllers

import models._
import play.api._
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
import play.api.mvc.BodyParsers._
import play.api.libs.json.Json
import play.api.libs.json.Json._

object Application extends Controller{
  
  //JSON read/write
  implicit val catFormat = Json.format[Cat]

  //create an instance of the table
  val Cats = TableQuery[CatsTable] //see a way to architect your app in the computers-database-slick sample

  def index = DBAction { implicit rs =>
    Ok(toJson(Cats.list))
  }

  def insert = DBAction(parse.json) { implicit rs =>
    rs.request.body.validate[Cat].map { cat =>
        Cats.insert(cat)
        Ok(toJson(cat))
    }.getOrElse(BadRequest("invalid json"))    
  }
  
}
