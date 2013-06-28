package models

import play.api.db.slick.Config.driver._

case class Cat(name: String, color: String)

class Cats extends Table[Cat]("CAT") {

  def name = column[String]("name", O.PrimaryKey)
  def color = column[String]("color", O.NotNull)

  def * = name ~ color <> (Cat.apply _, Cat.unapply _)
}
/** Separate package object since "package object models" is broken in Play < 2.2
  * In Play >= 2.2 this can be moved into package object models
  * @see https://github.com/playframework/Play20/issues/867
  */
package object tables{
  val Cats = new Cats
}