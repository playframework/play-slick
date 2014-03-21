package models

import play.api.db.slick.Config.driver.simple._

case class Cat(name: String, color: String)

/* Table mapping
 */
class CatsTable(tag: Tag) extends Table[Cat](tag, "CAT") {

  def name = column[String]("name", O.PrimaryKey)
  def color = column[String]("color", O.NotNull)

  def * = (name, color) <> (Cat.tupled, Cat.unapply _)
}
