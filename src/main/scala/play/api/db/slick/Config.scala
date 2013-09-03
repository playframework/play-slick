package play.api.db.slick

object Config {
  lazy val driver = DB(play.api.Play.current).driver
}
