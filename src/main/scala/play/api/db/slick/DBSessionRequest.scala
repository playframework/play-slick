package play.api.db.slick

import play.api.mvc.{ Request, AnyContent }

case class DBSessionRequest(session: Session, request: Request[AnyContent])
