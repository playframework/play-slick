package dao

import scala.concurrent.Future

import javax.inject.Inject
import models.Cat
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.profile.RelationalProfile

class CatDAO @Inject()(dbConfigProvider: DatabaseConfigProvider) {
  protected val (driver, db) = {
    val config = dbConfigProvider.get[RelationalProfile]
    (config.driver, config.db)
  }

  import driver.api._

  private val Cats = TableQuery[CatsTable]

  def all(): Future[Seq[Cat]] = db.run(Cats.result)

  def insert(cat: Cat): Future[Unit] = db.run(Cats += cat).map { _ => () }

  private class CatsTable(tag: Tag) extends Table[Cat](tag, "CAT") {

    def name = column[String]("NAME", O.PrimaryKey)
    def color = column[String]("COLOR")

    def * = (name, color) <> (Cat.tupled, Cat.unapply _)
  }
}