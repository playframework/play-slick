package tables

import slick.driver.JdbcProfile
import models.Cat

trait CatTable {
  protected val driver: JdbcProfile
  import driver.api._
  class Cats(tag: Tag) extends Table[Cat](tag, "CAT") {

    def name = column[String]("NAME", O.PrimaryKey)
    def color = column[String]("COLOR")

    def * = (name, color) <> ((Cat.apply _).tupled, Cat.unapply _)
  }
}