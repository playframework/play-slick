package play.api.db.slick

import play.api.mvc.Request
import play.api.mvc.WrappedRequest

case class DBSessionRequest[A](dbSession: Session, request: Request[A]) extends WrappedRequest[A](request)
