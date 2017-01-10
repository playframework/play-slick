package dao

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import slick.jdbc.JdbcProfile
import play.api.Play
import models.Record
import play.api.libs.streams.Streams
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Enumeratee
import play.api.libs.iteratee.Iteratee
import scala.concurrent.Future

class RecordsDAO extends HasDatabaseConfig[JdbcProfile] {
  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  import profile.api._

  /** Mapping of columns to the row object */
  class Records(tag: Tag) extends Table[Record](tag, "RECORDS") {
    def id = column[Int]("ID")
    def name = column[String]("NAME")
    def * = (id, name) <> ((Record.apply _).tupled, Record.unapply)
  }

  /** Base query for the table */
  object records extends TableQuery(new Records(_))

  /** Unexecuted Slick queries which may be composed by chaining.
    *
    * Only place methods here which return a not-yet executed Query or
    * (individually meaningful) Column. Methods placed here can be
    * chained/combined.
    */
  implicit class QueryExtensions(val q: Query[Records, Record, Seq]) {
    def names = q.map(_.name)
    def byId(id: Rep[Int]) = q.filter(_.id === id)
  }

  def all(): Future[Seq[Record]] = db.run(records.result)
  def count(): Future[Int] = db.run(records.length.result)
  def insert(records: Seq[Record]): Future[Option[Int]] = db.run(this.records ++= records)

  /** This is the interesting bit: enumerate the query for all Records in chunks of 2 */
  def enumerateAllInChunksOfTwo: Enumerator[List[Record]] = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    val input: Enumerator[Record] = Streams.publisherToEnumerator(db.stream(records.result))
    val slider: Enumeratee[Record, List[Record]] = Enumeratee.grouped[Record](Iteratee.takeUpTo[Record](2).map(_.toList))
    input.through(slider)
  }
}