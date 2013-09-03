package play.api.db.slick

import play.api.Application
import play.api.mvc._
import scala.concurrent.Future

object DBAction {
  import SlickExecutionContexts.executionContext

  def apply(resultFunction: => SimpleResult) = {
    Action.async {
      Future(resultFunction)(executionContext)
    }
  }

  def apply(requestHandler: DBSessionRequest => SimpleResult)(implicit app: Application) = {
    Action.async { implicit request =>
      Future {
        DB.withSession { session: Session =>
          requestHandler(DBSessionRequest(session, request))
        }
      }(executionContext)
    }
  }

}