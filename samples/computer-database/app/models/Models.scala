package models

import java.util.Date
import java.sql.{ Date => SqlDate }
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.Tag
import java.sql.Timestamp

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class Company(id: Option[Long], name: String)

case class Computer(id: Option[Long] = None, name: String, introduced: Option[Date] = None, discontinued: Option[Date] = None, companyId: Option[Long] = None)

class Companies(tag: Tag) extends Table[Company](tag, "COMPANY") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def * = (id.?, name) <> (Company.tupled, Company.unapply _)
}

object Companies {
  
  val companies = TableQuery[Companies]
  
  /**
   * Construct the Map[String,String] needed to fill a select options set
   */
  def options(implicit s: Session): Seq[(String, String)] = {
    val query = (for {
      company <- companies
    } yield (company.id, company.name)).sortBy(_._2)
    query.list.map(row => (row._1.toString, row._2))
  }

  /**
   * Insert a new company
   * @param company
   */
  def insert(company: Company)(implicit s: Session) {
    companies.insert(company)
  }
}

class Computers(tag: Tag) extends Table[Computer](tag, "COMPUTER") {

  implicit val dateColumnType = MappedColumnType.base[Date, Long](d => d.getTime, d => new Date(d))

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def introduced = column[Date]("introduced", O.Nullable)
  def discontinued = column[Date]("discontinued", O.Nullable)
  def companyId = column[Long]("companyId", O.Nullable)
  
  def * = (id.?, name, introduced.?, discontinued.?, companyId.?) <>(Computer.tupled, Computer.unapply _)
}

object Computers {
  
  val computers = TableQuery[Computers]
  val companies = TableQuery[Companies]
  
  /**
   * Retrieve a computer from the id
   * @param id
   */
  def findById(id: Long)(implicit s: Session): Option[Computer] =
    computers.where(_.id === id).firstOption

  /**
   * Count all computers
   */
  def count(implicit s: Session): Int =
    Query(computers.length).first

  /**
   * Count computers with a filter
   * @param filter
   */
  def count(filter: String)(implicit s: Session): Int =
    Query(computers.where(_.name.toLowerCase like filter.toLowerCase).length).first

  /**
   * Return a page of (Computer,Company)
   * @param page
   * @param pageSize
   * @param orderBy
   * @param filter
   */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%")(implicit s: Session): Page[(Computer, Option[Company])] = {

    val offset = pageSize * page
    val query =
      (for {
        (computer, company) <- computers leftJoin companies on (_.companyId === _.id)
        if computer.name.toLowerCase like filter.toLowerCase()
      } yield (computer, company.id.?, company.name.?))
        .drop(offset)
        .take(pageSize)

    val totalRows = count(filter)
    val result = query.list.map(row => (row._1, row._2.map(value => Company(Option(value), row._3.get))))

    Page(result, page, offset, totalRows)
  }

  /**
   * Insert a new computer
   * @param computer
   */
  def insert(computer: Computer)(implicit s: Session) {
    computers.insert(computer)
  }

  /**
   * Update a computer
   * @param id
   * @param computer
   */
  def update(id: Long, computer: Computer)(implicit s: Session) {
    val computerToUpdate: Computer = computer.copy(Some(id))
    computers.where(_.id === id).update(computerToUpdate)
  }

  /**
   * Delete a computer
   * @param id
   */
  def delete(id: Long)(implicit s: Session) {
    computers.where(_.id === id).delete
  }
}
