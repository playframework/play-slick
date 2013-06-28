package play.api.db
import play.api.mvc.{Request,AnyContent,AsyncResult,Result}
//import play.api._

package object slick{
  // database connectivity
  import play.api.Application
  def DB(implicit app:Application) = new Database()(app)
  def DB(name:String)(implicit app:Application) = new Database(name)(app)
  val driver = DB(play.api.Play.current).driver
  type Session = scala.slick.session.Session // for export to user app

  // session
  implicit def requestWithDbSession2request(implicit r:RequestWithDbSession): Request[AnyContent] = r.request
  implicit def requestWithDbSession2session(implicit r:RequestWithDbSession) : Session = r.session
  case class RequestWithDbSession( request:Request[AnyContent], session: Session )

  // async and database enabled Actions    
  import scala.concurrent.Future
  import akka.actor.ActorSystem
  import play.api.mvc.Action

  val executionContext = ActorSystem("slick-plugin-system").dispatchers.lookup("slick.execution-context")

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