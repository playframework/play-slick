package play.api.db

import play.api.mvc.Request
import play.api.Application
import scala.Predef._
import scala.language.higherKinds
import scala.language.implicitConversions

package object slick {
  //DB helpers that mimics play.api.db.DB
  def DB(implicit app: Application) = new Database(app = app)
  def DB(name: String)(implicit app: Application) = new Database(name, app)

  //for export to user app
  type Session = scala.slick.session.Session

  implicit def dbSessionRequestAsRequest[A](r: DBSessionRequest[A]): Request[A] = r.request
  implicit def dbSessionRequestAsSession[_](implicit r: DBSessionRequest[_]): Session = r.session

}
