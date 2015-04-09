package controllers

import dao.RecordsDAO
import play.api.libs.Comet
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumeratee
import play.api.libs.json.Json.toJson
import play.api.mvc.Action
import play.api.mvc.Controller

object Application extends Controller {
  def recordsDAO = new RecordsDAO

  def index = Action { request =>
    Ok(views.html.index())
  }

  def list = Action.async {
    recordsDAO.all().map(records => Ok(toJson(records)))
  }

  def listComet = Action {
    // Records fetched in chunks of 2, and asynchronously piped out to
    // browser in chunked http responses, to be handled by comet callback.
    //
    // see http://www.playframework.com/documentation/2.2.x/ScalaComet
    val pipeline = recordsDAO.enumerateAllInChunksOfTwo &>
      Enumeratee.map(toJson(_)) &>
      Comet(callback = "parent.cometMessage")

    Ok.chunked(pipeline)
  }
}