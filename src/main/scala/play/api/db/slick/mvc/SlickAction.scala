package play.api.db.slick.mvc

import play.api.mvc._  
import play.api._
import scala.concurrent._
import play.api.Play.current
import akka.actor.ActorSystem

trait SlickController { self: Controller =>

  val slickExecutionContext: ExecutionContext = ActorSystem("slick-plugin-system").dispatchers.lookup("slick.execution-context")

  def SlickAction(r: => Result) = {
    Action {
      Async {
        Future(r)(slickExecutionContext)
   	  }
	  }
	}

  def SlickAction(r: (Request[play.api.mvc.AnyContent]) => Result) = {
    Action { implicit request => 
      Async {
        Future(r(request))(slickExecutionContext)
      }
    }
  }

}