package models

import scala.concurrent.ExecutionContext

import play.api.db.slick.iteratees.SlickPlayIteratees._
import play.api.db.slick.Config.driver
import play.api.db.slick.Config.driver.simple._
import play.api.Logger

/** Row of data from the table "records" */
case class Record(id: Int, name: String)

/** Mapping of columns to the row object */
class Records(tag: Tag) extends Table[Record](tag, "records") {
  def id   = column[Int   ]("id")
  def name = column[String]("name")
  def * = (id, name) <> (Record.tupled, Record.unapply)
}

object Records {
  /** Base query for the table */
  object records extends TableQuery(new Records(_))

  /** Unexecuted Slick queries which may be composed by chaining.
    *
    * Only place methods here which return a not-yet executed Query or
    * (individually meaningful) Column. Methods placed here can be
    * chained/combined.
    */
  implicit class QueryExtensions(val q: Query[Records, Record]) {
    def names = q.map(_.name)
    def byId(id: Column[Int]) = q.filter(_.id === id)
  }

  /** Queries are executed here using the implicit session */
  case class DAO(implicit s: Session, ec: ExecutionContext) {
    def all = records.list
    def count = records.length.run

    /** This is the interesting bit: enumerate the query for all Records in chunks of 2 */
    def enumerateAllInChunksOfTwo = enumerateSlickQuery(
      driver,
      Right(s.database),
      records,
      maybeChunkSize = Some(2),
      logCallback = PlayLogCallback(
        Logger /*,
        shouldLogSqlOnSuccess = true */ )) // uncomment to log SQL even on success
  }

}
