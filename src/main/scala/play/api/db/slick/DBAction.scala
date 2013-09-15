package play.api.db.slick

import play.api.Application
import scala.concurrent.Future
import play.api.mvc.{AnyContent, BodyParser, Action, SimpleResult}
import play.api.mvc.BodyParsers.parse.anyContent

object DBAction {
  import SlickExecutionContexts.executionContext

  def apply(resultFunction: => SimpleResult) = {
    Action.async {
      Future(resultFunction)(executionContext)
    }
  }

  def transaction(requestHandler: DBSessionRequest[AnyContent] => SimpleResult)(implicit app: Application) = {
    applyForDB(DB)(requestHandler)(anyContent)(DB.withTransaction)
  }

  def apply(requestHandler: DBSessionRequest[AnyContent] => SimpleResult)(implicit app: Application) = {
    applyForDB(DB)(requestHandler)(anyContent)(DB.withSession)
  }

  def apply[A](bodyParser:BodyParser[A])(requestHandler: DBSessionRequest[A] => SimpleResult)(implicit app: Application) = {
    applyForDB(DB)(requestHandler)(bodyParser)(DB.withSession)
  }

  def transaction[A](bodyParser:BodyParser[A])(requestHandler: DBSessionRequest[A] => SimpleResult)(implicit app: Application) = {
    applyForDB(DB)(requestHandler)(bodyParser)(DB.withTransaction)
  }

  def apply[A](dbName: String)(bodyParser:BodyParser[A])(requestHandler: DBSessionRequest[A] => SimpleResult)(implicit app: Application) = {
    val db = DB(dbName)
    applyForDB(db)(requestHandler)(bodyParser)(db.withSession)
  }

  def transaction[A](dbName: String)(bodyParser:BodyParser[A])(requestHandler: DBSessionRequest[A] => SimpleResult)(implicit app: Application) = {
    val db = DB(dbName)
    applyForDB(db)(requestHandler)(bodyParser)(db.withTransaction)
  }

  private def applyForDB[A](db: Database)(requestHandler: DBSessionRequest[A] => SimpleResult)(bodyParser:BodyParser[A])
                             (f : (Session => SimpleResult) => SimpleResult)(implicit app: Application) = {
    Action.async(bodyParser) { implicit request =>
      Future {
        f { session: Session =>
          requestHandler(DBSessionRequest(session, request))
        }
      }(executionContext)
    }
  }

}