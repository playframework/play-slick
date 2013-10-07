package play.api.db.slick

import play.api.mvc.Request

case class DBSessionRequest[A](session: Session, request: Request[A])
