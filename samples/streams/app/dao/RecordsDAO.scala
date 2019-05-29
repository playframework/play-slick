package dao

import javax.inject.Inject

import com.google.inject.AbstractModule
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import slick.jdbc.JdbcProfile
import models.Record
import slick.basic.DatabaseConfig

import scala.concurrent.{ ExecutionContext, Future }

import akka.NotUsed
import akka.stream.scaladsl.Source

class RecordsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfig[JdbcProfile] {

  import profile.api._

  override val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  /** Mapping of columns to the row object */
  class Records(tag: Tag) extends Table[Record](tag, "RECORDS") {
    def id = column[Int]("ID")

    def name = column[String]("NAME")

    def * = (id, name) <> ((Record.apply _).tupled, Record.unapply)
  }

  /** Base query for the table */
  object records extends TableQuery(new Records(_))

  /**
   * Non-executed Slick queries which may be composed by chaining.
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
  def streamInChunksOf(size: Int = 2): Source[Seq[Record], NotUsed] = {
    Source.fromPublisher(db.stream(records.result)).grouped(2)
  }
}

class RecordsDAOModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[RecordsDAO]).asEagerSingleton()
  }
}