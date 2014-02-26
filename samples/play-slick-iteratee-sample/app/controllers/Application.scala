package controllers

import play.api.mvc._
import play.api.libs.iteratee.Enumeratee
import play.api.libs.json.Json.toJson
import play.api.libs.json.Writes.traversableWrites
import play.api.libs.Comet
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import models._

object Application extends Controller {
  implicit val recordsWrites = traversableWrites(records.RecordWrites)

  def index = Action { request =>
    Ok(views.html.index())
  }

  def list = Action { request =>
    Ok(toJson(records.all))
  }

  def listComet = Action { request =>
    // Records fetched in chunks of 2, and asynchronously piped out to
    // browser in chunked http responses, to be handled by comet callback.
    //
    // see http://www.playframework.com/documentation/2.2.x/ScalaComet
    val pipeline = records.enumerateAllInChunksOfTwo &>
      Enumeratee.map(toJson(_)) &>
      Comet(callback = "parent.cometMessage")

    Ok.chunked(pipeline)
  }

}