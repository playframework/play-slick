package models

import slick.driver.ExtendedProfile
import play.api.db.slick.Profile
import play.api.db.slick.DB

class DAO(override val profile: ExtendedProfile) extends CatComponent with Profile

object current {
  val dao = new DAO(DB(play.api.Play.current).driver)
}
