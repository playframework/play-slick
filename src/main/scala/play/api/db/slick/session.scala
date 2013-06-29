package play.api.db.slick
import play.api.mvc._
package object session{
  implicit def requestWithDbSession2request(implicit r:RequestWithDbSession): Request[AnyContent] = r.request
  implicit def requestWithDbSession2session(implicit r:RequestWithDbSession) : slick.session.Session = r.session
  case class RequestWithDbSession( request:Request[AnyContent], session: slick.session.Session )
}
