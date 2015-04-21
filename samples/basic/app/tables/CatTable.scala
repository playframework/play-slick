package tables

import models.Cat
import slick.profile.RelationalProfile

trait CatTable {
  protected val driver: RelationalProfile
  import driver.api._
  class Cats(tag: Tag) extends Table[Cat](tag, "CAT") {

    def name = column[String]("NAME", O.PrimaryKey)
    def color = column[String]("COLOR")

    def * = (name, color) <> (Cat.tupled, Cat.unapply _)
  }
}