package play.api.db

import scala.language.higherKinds
import scala.language.implicitConversions

import play.api.Application

package object slick {
  // DB helpers that mimic play.api.db.DB
  def DB(implicit app: Application) = Database()(app)
  def DB(name: String)(implicit app: Application) = Database(name)(app)

  // Type alias for export to user app
  type Session = scala.slick.jdbc.JdbcBackend#Session

  implicit def dbSessionRequestAsSession[_](implicit r: DBSessionRequest[_]): Session = r.dbSession
}
