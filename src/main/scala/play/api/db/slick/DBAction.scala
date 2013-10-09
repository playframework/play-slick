package play.api.db.slick

import play.api.Application
import scala.concurrent.Future
import play.api.mvc.{ AnyContent, BodyParser, Action, SimpleResult }
import play.api.mvc.BodyParsers.parse.anyContent
import scala.concurrent.ExecutionContext
import java.util.concurrent.ThreadPoolExecutor
import play.api.mvc.Result
import play.api.Mode
import play.api.Configuration

case class DBAttributes(executionContext: ExecutionContext, maybeThreadPool: Option[ThreadPoolExecutor], minConnections: Int, maxConnections: Int, partions: Int, maxQueriesPerRequest: Int)

/**
 * Use the DBAction when you want to use the default settings in your controller.
 *
 * {{{
 * def index = DBAction{ implicit rs =>
 *   Ok(MyStuff.list())
 * }
 * }}}
 *
 * By default it uses the database named by default in the configuration in all modes (Prod, Dev)
 * except in test, where it uses test.
 *
 * It is possible to define a maximum amount queries per request that can be handled at a given point
 * to avoid overloading the application (user gets the error page instead of seeing a slow app).
 *
 * The maximum amount of per requests  is read from:
 * db.<db-name>.maxQueriesPerRequest
 *
 * If you need to override the default behavior, create a new object
 * with the DefaultDBAction trait.
 * {{{
 *  class AnotherDBAction(myApp: Application, myErrorPage: Result) extends DefaultDBAction {
 *    override lazy val app = myApp
 *    override val errorPage = myErrorPage
 *  }
 * }}}
 */
object DBAction extends DefaultDBAction

/**
 * Defines the DBAction default behaviour
 *
 */
trait DefaultDBAction extends PredicatedDBAction {
  /** Override to use a different app */
  protected lazy val app = play.api.Play.current

  /** Override to change default error page */
  protected val errorPage = play.api.mvc.Results.ServiceUnavailable

  /** Used to includeFilter and to determine defaultName */
  protected lazy val testName = "test"

  /** The default name used by the apply action that has a default name */
  override lazy val defaultName = {
    if (app.mode == Mode.Test && app.configuration.getConfig(s"db.$testName").isDefined) testName
    else Database.defaultName
  }

  /** Used to determine which db name config sections are to be included */
  protected def includeFilter(name: String) = {
    if (app.mode == Mode.Test && app.configuration.getConfig(s"db.$testName").isDefined) name == testName //only use test in test mode
    else name != testName //never use test in 
  }

  /**
   * A Map containing all the information needed to created cached functions that check DB availability
   */
  override lazy val attributes: Map[String, DBAttributes] = {
    val dbNames = app.configuration.getConfig("db").toSet.flatMap { section: Configuration => section.subKeys }
    val filtered = dbNames.filter(includeFilter)
    filtered.map { dbName =>
      val paritionCount = app.configuration.getInt(s"db.$dbName.partitionCount").getOrElse(2)
      val maxConnections = app.configuration.getInt(s"db.$dbName.maxConnectionsPerPartition").getOrElse(5)
      val minConnections = app.configuration.getInt(s"db.$dbName.minConnectionsPerPartition").getOrElse(5)
      val maxQueriesPerRequest = app.configuration.getInt(s"db.$dbName.maxQueriesPerRequest").getOrElse(20)
      val (executionContext, threadPool) = SlickExecutionContext.threadPoolExecutionContext(minConnections, maxConnections)
      dbName -> DBAttributes(executionContext, Some(threadPool), minConnections, maxConnections, paritionCount, maxQueriesPerRequest)
    }(collection.breakOut)
  }
}

trait PredicatedDBAction {
  val defaultName: String

  val attributes: Map[String, DBAttributes]

  /** Defines the functions used to check db availability for each db name */
  protected lazy val available: Map[String, () => Boolean] = {
    attributes.map {
      case (name, dbAttributes) =>
        name -> {
          val maxConnections = dbAttributes.maxConnections
          val maxQueriesPerRequest = dbAttributes.maxQueriesPerRequest
          dbAttributes.maybeThreadPool.map { threadPool =>
            () => threadPool.getQueue.size() < maxConnections * maxQueriesPerRequest
          }.getOrElse {
            () => true
          }
        }
    }
  }

  protected def isDBAvailable(name: String): Boolean = {
    available(name)()
  }

  protected val errorPage: Result

  def apply(resultFunction: => SimpleResult): Action[AnyContent] = {
    if (isDBAvailable(defaultName)) {
      Action.async {
        Future(resultFunction)(attributes(defaultName).executionContext)
      }
    } else Action(errorPage)
  }

  def apply(requestHandler: DBSessionRequest[_] => SimpleResult)(implicit app: Application) = {
    applyForDB(DB)(requestHandler)(anyContent)(DB.withSession)(errorPage)
  }
  
  def apply[A](bodyParser: BodyParser[A])(requestHandler: DBSessionRequest[A] => SimpleResult)(implicit app: Application) = {
    applyForDB(DB)(requestHandler)(bodyParser)(DB.withSession)(errorPage)
  }
  
  def apply[A](dbName: String, bodyParser: BodyParser[A] = anyContent)(requestHandler: DBSessionRequest[A] => SimpleResult)(implicit app: Application) = {
    val db = DB(dbName)
    applyForDB(db)(requestHandler)(bodyParser)(db.withSession)(errorPage)
  }

  def transaction(requestHandler: DBSessionRequest[AnyContent] => SimpleResult)(implicit app: Application) = {
    applyForDB(DB)(requestHandler)(anyContent)(DB.withTransaction)(errorPage)
  }

  def transaction[A](bodyParser: BodyParser[A])(requestHandler: DBSessionRequest[A] => SimpleResult)(implicit app: Application) = {
    applyForDB(DB)(requestHandler)(bodyParser)(DB.withTransaction)(errorPage)
  }

  def transaction[A](dbName: String, bodyParser: BodyParser[A] = anyContent)(requestHandler: DBSessionRequest[A] => SimpleResult)(implicit app: Application) = {
    val db = DB(dbName)
    applyForDB(db)(requestHandler)(bodyParser)(db.withTransaction)(errorPage)
  }
  
  protected def applyForDB[A](db: Database)(requestHandler: DBSessionRequest[A] => SimpleResult)(bodyParser: BodyParser[A])(f: (Session => SimpleResult) => SimpleResult)(errorPage: => Result)(implicit app: Application, executionContext: ExecutionContext = attributes(defaultName).executionContext): Action[A] = {
    if (isDBAvailable(db.name)) {
      Action.async(bodyParser) { implicit request =>
        Future {
          f { session: Session =>
            requestHandler(DBSessionRequest(session, request))
          }
        }(executionContext)
      }
    } else Action(bodyParser) { _ => errorPage }
  }

}