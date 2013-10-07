package play.api.db

import play.api.mvc.Request
import play.api.Application
import scala.Predef._
import scala.language.higherKinds
import scala.language.implicitConversions
import play.api.mvc.Flash

package object slick {
  //DB helpers that mimics play.api.db.DB
  def DB(implicit app: Application) = Database()(app)
  def DB(name: String)(implicit app: Application) = Database(name)(app)

  //for export to user app
  type Session = scala.slick.jdbc.JdbcBackend#Session

  implicit def dbSessionRequestAsSession[_](implicit r: DBSessionRequest[_]): Session = r.session
  implicit def dbSessionRequestAsRequest[A](r: DBSessionRequest[A]): Request[A] = r.request
}
