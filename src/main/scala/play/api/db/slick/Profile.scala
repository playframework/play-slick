package play.api.db.slick

import scala.slick.driver.ExtendedProfile

/**
  * This profile makes it easier to use 
  * the cake pattern with Slick
  * 
  * Check out the CatComponent and DAO in the 
  * sample application
  */
trait Profile {
  val profile: ExtendedProfile
}
