package models

import scala.slick.driver.JdbcProfile
import scala.slick.lifted.TableQuery
import play.api.db.slick.{ Config, Profile }

class DAO(override val profile: JdbcProfile) extends CatComponent with Profile {
  val Cats = TableQuery[CatsTable]
}

object current {
  val dao = new DAO(Config.driver)
}
