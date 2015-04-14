package dao

import scala.concurrent.Future

import javax.inject.Inject
import models.Dog
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.db.NamedDatabase
import slick.profile.RelationalProfile

class DogDAO @Inject()(@NamedDatabase("mydb") dbConfigProvider: DatabaseConfigProvider) {
  protected val (driver, db) = {
    val config = dbConfigProvider.get[RelationalProfile]
    (config.driver, config.db)
  }

  import driver.api._

  private val Dogs = TableQuery[DogsTable]

  def all(): Future[Seq[Dog]] = db.run(Dogs.result)

  def insert(cat: Dog): Future[Unit] = db.run(Dogs += cat).map { _ => () }

  private class DogsTable(tag: Tag) extends Table[Dog](tag, "DOG") {

    def name = column[String]("NAME", O.PrimaryKey)
    def color = column[String]("COLOR")

    def * = (name, color) <> (Dog.tupled, Dog.unapply _)
  }
}