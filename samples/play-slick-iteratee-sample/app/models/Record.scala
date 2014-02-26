package models

import play.api.db.DB
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, JsValue, Writes}
import play.api.libs.json.Json._
import play.api.Logger
import play.api.Play.current
import scala.slick.driver.H2Driver
import scala.slick.driver.H2Driver.simple._

import play.api.db.slick.iteratees.SlickPlayIteratees._

case class Record(id: Int, name: String)

class Records(tag: Tag) extends Table[Record](tag, "records") {
  def id   = column[Int   ]("id")
  def name = column[String]("name")
  def * = (id, name) <> (Record.tupled, Record.unapply)
}

object records extends TableQuery(new Records(_)) {
  def database = Database.forDataSource(DB.getDataSource("default"))
  lazy val profile = H2Driver

  def all = database withSession { implicit s => this.list }

  /** This is it: enumerate the query for all Records in chunks of 2 */
  def enumerateAllInChunksOfTwo = enumerateSlickQuery(profile, Right(database), this, maybeChunkSize = Some(2),
    logCallback = PlayLogCallback(Logger /*, shouldLogSqlOnSuccess = true */ )) // uncomment to log SQL on successful fetches

  // serialize Record to json
  implicit object RecordWrites extends Writes[Record] {
    def writes(r: Record): JsValue = JsObject(Seq("id" -> toJson(r.id), "name" -> toJson(r.name)))
  }

}
