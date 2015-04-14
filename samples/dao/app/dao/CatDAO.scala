package dao

import scala.concurrent.Future

import models.Cat
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.profile.RelationalProfile

class CatDAO {
  protected val (driver, db) = {
    val config = DatabaseConfigProvider.get[RelationalProfile](Play.current)
    (config.driver, config.db)
  }

  import driver.api._

  private val Cats = TableQuery[CatsTable]

  def all(): Future[List[Cat]] = db.run(Cats.result).map(_.toList)

  def insert(cat: Cat): Future[Unit] = db.run(Cats += cat).map(_ => ())

  private class CatsTable(tag: Tag) extends Table[Cat](tag, "CAT") {

    def name = column[String]("NAME", O.PrimaryKey)
    def color = column[String]("COLOR")

    def * = (name, color) <> (Cat.tupled, Cat.unapply _)
  }
}