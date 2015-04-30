package play.api.db

package object slick {
  case class DbName(val value: String) extends AnyVal

  val IssueTracker = "https://github.com/playframework/play-slick/issues"
}