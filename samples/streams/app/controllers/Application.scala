package controllers

import javax.inject.Inject

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import dao.RecordsDAO
import play.api.http.ContentTypes
import play.api.libs.Comet
import play.api.libs.json.Json.toJson
import play.api.mvc.{ AbstractController, ControllerComponents }

import scala.concurrent.ExecutionContext

class Application @Inject() (recordsDAO: RecordsDAO, components: ControllerComponents)(implicit materializer: Materializer, executionContext: ExecutionContext) extends AbstractController(components) {

  def index = Action {
    Ok(views.html.index())
  }

  def list = Action.async {
    recordsDAO.all().map(records => Ok(toJson(records)))
  }

  def listComet = Action {
    // Records fetched in chunks of 2, and asynchronously piped out to
    // browser in chunked http responses, to be handled by comet callback.
    //
    // see https://www.playframework.com/documentation/2.7.x/ScalaComet
    val source = recordsDAO.streamInChunksOf(2).map(records => toJson(records))
    Ok.chunked(source via Comet.json("parent.cometMessage")).as(ContentTypes.HTML)
  }
}