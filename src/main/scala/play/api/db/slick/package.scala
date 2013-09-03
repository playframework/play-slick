package play.api.db

import play.api.mvc.{ Request, AnyContent }
import play.api.Application

package object slick {
  //DB helpers that mimics play.api.db.DB
  def DB(implicit app: Application) = new Database(app = app)
  def DB(name: String)(implicit app: Application) = new Database(name, app)

  //for export to user app
  type Session = scala.slick.session.Session

  //implicit / automatic transforms of DBSessionRequest to Request and Session
  implicit def dbSessionRequestAsRequest(implicit r: DBSessionRequest): Request[AnyContent] = r.request
  implicit def dbSessionRequestAsSession(implicit r: DBSessionRequest): Session = r.session
}
