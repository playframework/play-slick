package dao

import scala.concurrent.Future

import javax.inject.Inject
import models.Dog
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.H2Driver.profile.api._

class DogDAO(db: Database) {
  private val Dogs = TableQuery[DogsTable]

  def all(): Future[Seq[Dog]] = db.run(Dogs.result)

  def insert(dog: Dog): Future[Unit] = db.run(Dogs += dog).map { _ => () }

  private class DogsTable(tag: Tag) extends Table[Dog](tag, "DOG") {

    def name = column[String]("NAME", O.PrimaryKey)
    def color = column[String]("COLOR")

    def * = (name, color) <> (Dog.tupled, Dog.unapply _)
  }
}
