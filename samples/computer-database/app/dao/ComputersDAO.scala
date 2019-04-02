package dao

import java.util.Date
import javax.inject.{ Inject, Singleton }

import models.{ Company, Computer, Page }
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton()
class ComputersDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends CompaniesComponent
  with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  class Computers(tag: Tag) extends Table[Computer](tag, "COMPUTER") {

    implicit val dateColumnType = MappedColumnType.base[Date, Long](d => d.getTime, d => new Date(d))

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def introduced = column[Option[Date]]("INTRODUCED")
    def discontinued = column[Option[Date]]("DISCONTINUED")
    def companyId = column[Option[Long]]("COMPANY_ID")

    def * = (id.?, name, introduced, discontinued, companyId) <> (Computer.tupled, Computer.unapply _)
  }

  private val computers = TableQuery[Computers]
  private val companies = TableQuery[Companies]

  /** Retrieve a computer from the id. */
  def findById(id: Long): Future[Option[Computer]] =
    db.run(computers.filter(_.id === id).result.headOption)

  /** Count all computers. */
  def count(): Future[Int] = {
    // this should be changed to
    // db.run(computers.length.result)
    // when https://github.com/slick/slick/issues/1237 is fixed
    db.run(computers.map(_.id).length.result)
  }
  /** Count computers with a filter. */
  def count(filter: String): Future[Int] = {
    db.run(computers.filter { computer => computer.name.toLowerCase like filter.toLowerCase }.length.result)
  }

  /** Return a page of (Computer,Company) */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[(Computer, Company)]] = {

    val offset = pageSize * page
    val query =
      (for {
        (computer, company) <- computers joinLeft companies on (_.companyId === _.id)
        if computer.name.toLowerCase like filter.toLowerCase
      } yield (computer, company.map(_.id), company.map(_.name)))
        .drop(offset)
        .take(pageSize)

    for {
      totalRows <- count(filter)
      list = query.result.map { rows => rows.collect { case (computer, id, Some(name)) => (computer, Company(id, name)) } }
      result <- db.run(list)
    } yield Page(result, page, offset, totalRows)
  }

  /** Insert a new computer. */
  def insert(computer: Computer): Future[Unit] =
    db.run(computers += computer).map(_ => ())

  /** Insert new computers. */
  def insert(computers: Seq[Computer]): Future[Unit] =
    db.run(this.computers ++= computers).map(_ => ())

  /** Update a computer. */
  def update(id: Long, computer: Computer): Future[Unit] = {
    val computerToUpdate: Computer = computer.copy(Some(id))
    db.run(computers.filter(_.id === id).update(computerToUpdate)).map(_ => ())
  }

  /** Delete a computer. */
  def delete(id: Long): Future[Unit] =
    db.run(computers.filter(_.id === id).delete).map(_ => ())

}