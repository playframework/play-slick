package play.api.db.slick.mvc

import play.api.mvc._  
import play.api._
import scala.concurrent._
import play.api.Play.current
import play.api.db.slick._
import play.api.db.slick.session._

trait DBController { self: Controller =>
  val slickExecutionContext = SlickExecutionContext.executionContext

  def DBAction(r: => Result) = {
    Action {
      Async {
        Future(r)(slickExecutionContext)
   	  }
	  }
	}

  def DBAction(r: (RequestWithDbSession) => Result) = {
    Action { implicit request => 
      Async {
        DB.database.withSession{ s:slick.session.Session =>
          Future(r( RequestWithDbSession(request,s) ))(slickExecutionContext)
        }
      }
    }
  }

}