package play.api.db

import play.api.mvc.{Request,AnyContent}

case class RequestWithDbSession( request:Request[AnyContent], session: slick.Session )

package object slick{
  // database connectivity
  import play.api.Application
  def DB(implicit app:Application) = new Database("default",app)
  def DB(name:String)(implicit app:Application) = new Database(name,app)
  object Config{
    lazy val driver = DB(play.api.Play.current).driver    
  }
  type Session = scala.slick.session.Session // for export to user app

  // session
  implicit def requestWithDbSession2request(implicit r:RequestWithDbSession): Request[AnyContent] = r.request
  implicit def requestWithDbSession2session(implicit r:RequestWithDbSession) : Session = r.session

  // async and database enabled Actions    
  import play.api.libs.concurrent.Akka
  lazy val executionContext = {
    val app = play.api.Play.current
    val configSection = "akka.actor.slick-context"
    app.configuration.getConfig(configSection) match { //TODO: create a better default execution context
      case Some(_) => Akka.system(app).dispatchers.lookup(configSection)
      case None => play.api.libs.concurrent.Execution.defaultContext
    }
  }

  import scala.concurrent.Future
  import play.api.mvc.{AsyncResult,Result,Action}
  object DBAction{
  	def apply(r: => Result) = {
      Action {
        AsyncResult {
          Future(r)(executionContext)
     	  }
      }
    }
    def apply(r: (RequestWithDbSession) => Result)(implicit app:Application) = {
      Action { implicit request => 
        AsyncResult {
          DB.withSession{ s:scala.slick.session.Session =>
            Future(r( RequestWithDbSession(request,s) ))(executionContext)
          }
        }
      }
    }
  }
}
