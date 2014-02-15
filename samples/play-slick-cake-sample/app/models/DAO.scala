package models

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery
import play.api.db.slick.Profile
import play.api.db.slick.DB

class DAO(override val profile: JdbcProfile) extends CatComponent with Profile {
  val Cats = TableQuery[CatsTable]
}

object current {
  val dao = new DAO(DB(play.api.Play.current).driver)
}
