package controllers

import play.api.mvc._
import play.api.db.slick._
import play.api.libs.iteratee.Enumeratee
import play.api.libs.json._
import play.api.libs.json.Json.toJson
import play.api.libs.Comet

import models._

object Application extends Controller {
  implicit val recordWrites = Json.writes[Record]

  def index = Action { request =>
    Ok(views.html.index())
  }

  def list = DBAction { implicit rs =>
    Ok(toJson(Records.DAO().all))
  }

  def listComet = DBAction { implicit rs =>
    // Records fetched in chunks of 2, and asynchronously piped out to
    // browser in chunked http responses, to be handled by comet callback.
    //
    // see http://www.playframework.com/documentation/2.2.x/ScalaComet
    val pipeline = Records.DAO().enumerateAllInChunksOfTwo &>
      Enumeratee.map(toJson(_)) &>
      Comet(callback = "parent.cometMessage")

    Ok.chunked(pipeline)
  }

}