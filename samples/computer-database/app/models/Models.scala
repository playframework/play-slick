package models

import java.util.Date

import play.api.Play.current

import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import slick.lifted.{Join, MappedTypeMapper}

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class Company(id: Option[Long], name: String)

case class Computer(id: Option[Long] = None, name: String, introduced: Option[Date]= None, discontinued: Option[Date]= None, companyId: Option[Long]=None)

object Companies extends Table[Company]("COMPANY") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def * = id.? ~ name <>(Company.apply _, Company.unapply _)
  def autoInc = * returning id

  def options: Seq[(String, String)] = DB.withSession {
    implicit session =>
      val query = (for {
        company <- Companies
      } yield (company.id, company.name)
        ).sortBy(_._2)
      query.list.map(row => (row._1.toString, row._2))
  }

  def insert(company: Company) {
    DB.withSession {
      implicit session =>
        Companies.autoInc.insert(company)
    }
  }
}

object Computers extends Table[Computer]("COMPUTER") {



  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def introduced = column[Date]("introduced", O.Nullable)
  def discontinued = column[Date]("discontinued", O.Nullable)
  def companyId = column[Long]("companyId", O.Nullable)

  def * = id.? ~ name ~ introduced.? ~ discontinued.? ~ companyId.? <>(Computer.apply _, Computer.unapply _)

  def autoInc = * returning id

  val byId = createFinderBy(_.id)

  def findById(id: Long): Option[Computer] = DB.withSession {
    implicit session =>
      Computers.byId(id).firstOption
  }

  def count: Int = DB.withSession {
    implicit session =>
      Query(Computers.length).first
  }

  def count(filter: String) : Int = DB.withSession {
    implicit session =>
      Query(Computers.where(_.name.toLowerCase like filter.toLowerCase).length).first
  }

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Page[(Computer, Option[Company])] = {

    val offset = pageSize * page

    DB.withSession {
      implicit session =>
        val query =
          (for {
            (computer, company) <- Computers leftJoin Companies on (_.companyId === _.id)
            if computer.name.toLowerCase like filter.toLowerCase()
          }
          yield (computer, company.id.?, company.name.?))
            .drop(offset)
            .take(pageSize)

        val totalRows = count(filter)
        val result = query.list.map(row => (row._1, row._2.map(value => Company(Option(value), row._3.get))))

        Page(result, page, offset, totalRows)
    }
  }

  def insert(computer: Computer) {
   DB.withSession {
    implicit session =>
      Computers.autoInc.insert(computer)
    }
  }

  def update(id: Long, computer: Computer) {
    DB.withSession {
      implicit session =>
        val computerToUpdate: Computer = computer.copy(Some(id))
        Query(Computers).where(_.id === id).update(computerToUpdate)
    }
  }

  def delete(id: Long) {
    DB.withSession {
      implicit session =>
        Computers.where(_.id === id).mutate(_.delete)
    }
  }
}



