package play.api.db
package object slick{
  // database connectivity
  import play.api.Application
  def DB(implicit app:Application) = new Database("default",app)
  def DB(name:String)(implicit app:Application) = new Database(name,app)
  object Config{
    val driver = DB(play.api.Play.current).driver    
  }
  type Session = scala.slick.session.Session // for export to user app

  // session
  import play.api.mvc.{Request,AnyContent}
  implicit def requestWithDbSession2request(implicit r:RequestWithDbSession): Request[AnyContent] = r.request
  implicit def requestWithDbSession2session(implicit r:RequestWithDbSession) : Session = r.session
  case class RequestWithDbSession( request:Request[AnyContent], session: Session )

  // async and database enabled Actions    
  import play.api.libs.concurrent.Akka
  val executionContext = Akka.system(play.api.Play.current).dispatchers.lookup("akka.actor.slick-context")

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