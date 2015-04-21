package dao

import scala.concurrent.Future

import models.Company
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile

trait CompaniesComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class Companies(tag: Tag) extends Table[Company](tag, "COMPANY") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def * = (id.?, name) <> (Company.tupled, Company.unapply _)
  }
}

class CompaniesDAO extends CompaniesComponent with HasDatabaseConfig[JdbcProfile] {
  protected val dbConfig =  DatabaseConfigProvider.get[JdbcProfile](Play.current)

  import driver.api._

  val companies = TableQuery[Companies]

  /** Construct the Map[String,String] needed to fill a select options set */
  def options(): Future[Seq[(String, String)]] = {
    val query = (for {
      company <- companies
    } yield (company.id, company.name)).sortBy(/*name*/_._2)

    db.run(query.result).map(rows => rows.map { case (id, name) => (id.toString, name) })
  }

  /** Insert a new company */
  def insert(company: Company): Future[Unit] =
    db.run(companies += company).map(_ => ())

  /** Insert new companies */
  def insert(companies: Seq[Company]): Future[Unit] =
    db.run(this.companies ++= companies).map(_ => ())
}