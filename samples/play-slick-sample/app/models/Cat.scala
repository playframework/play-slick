package models

import play.api.db.slick.Config.driver._

case class Cat(name: String, color: String)

class Cats extends Table[Cat]("CAT") {

  def name = column[String]("name", O.PrimaryKey)
  def color = column[String]("color", O.NotNull)

  def * = name ~ color <> (Cat.apply _, Cat.unapply _)
}