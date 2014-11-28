package play.api.db.slick

import scala.concurrent.ExecutionContext

import play.api.mvc.Request
import play.api.mvc.WrappedRequest

/** Wrapped request with added db-specific information.
  *
  * See the slick package object for implicit functions to extract this information.
  */
case class DBSessionRequest[A](dbSession: Session, dbExecutionContext: ExecutionContext, request: Request[A]) extends WrappedRequest[A](request)
