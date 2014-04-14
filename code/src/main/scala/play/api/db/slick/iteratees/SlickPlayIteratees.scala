package play.api.db.slick.iteratees

import scala.concurrent.{ExecutionContext, Future}

import org.joda.time.DateTime
import play.api.libs.iteratee.{Enumeratee, Enumerator}
import play.api.LoggerLike
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.JdbcBackend
import scala.slick.lifted.Query
import scala.slick.jdbc.SessionWithAsyncTransaction


object SlickPlayIteratees {

  /** Default chunk size for queries */
  val DefaultQueryChunkSize: Int = 100

  /** Fields passed to callback after each query execution */
  case class LogFields(startTime: DateTime,
                       endTime: DateTime,
                       offset: Int,
                       maybeNumResults: Option[Int],      // [success cases only] number of results in chunk
                       maybeSqlStmt: Option[String],      // sql select statement unless generation throws exception
                       maybeException: Option[Throwable]) // [failure cases only] thrown exception

  /** A LogCallback is defined as an effect taking LogFields as input */
  type LogCallback = LogFields => Unit

  /** Default LogCallback does nothing */
  val EmptyLogCallback: LogCallback = LogFields => ()

  /** Create a LogCallback which logs to a Play framework Logger.
    * @param logger                the Play Framework logger
    * @param shouldLogSqlOnSuccess change this to `true` to log SQL statements on success as well
    */
  def PlayLogCallback(logger: LoggerLike, shouldLogSqlOnSuccess: Boolean = false): LogCallback = { fields: LogFields =>
    val durationMs = fields.endTime.getMillis - fields.startTime.getMillis
    val shouldLogSql = fields.maybeException.isDefined || shouldLogSqlOnSuccess

    // Log zero results in the success case where no more results available
    val maybeNumResults = fields.maybeNumResults.orElse(Some(0).filter(_ => fields.maybeException.isEmpty))

    val message = "enumerateSlickQuery - %s chunk in %d ms: offset %d%s%s".format(
      if (fields.maybeException.isEmpty) "fetched" else "failed to fetch",
      durationMs,
      fields.offset,
      maybeNumResults.map(n => ", %d records".format(n)).getOrElse(""),
      fields.maybeSqlStmt.filter(_ => shouldLogSql).map(s => " [" + s + "]").getOrElse(""))

    fields.maybeException match {
      case None     => logger.info(message)
      case Some(ex) => logger.error(message, ex)
    }
  }

  /** Returns a Play Enumerator which fetches the results of the given Slick query in chunks.
    *
    * @param sessionOrDatabase   Provide either a session (useful for consistent reads across a
    *                            larger transaction), or a database with which to create a session.
    *                            NOTE: closes the transaction on the session regardless of whether
    *                              it was passed in or created from a database.
    */
  def enumerateSlickQuery[Q, E, R](driverProfile: JdbcProfile,
                                   sessionOrDatabase: Either[SessionWithAsyncTransaction, JdbcBackend#Database],
                                   query: Query[Q, R],
                                   maybeChunkSize: Option[Int] = Some(DefaultQueryChunkSize),
                                   logCallback: LogCallback = EmptyLogCallback)(implicit ec: ExecutionContext): Enumerator[List[R]] = {
    maybeChunkSize.filter(_ <= 0).foreach { _ => throw new IllegalArgumentException("chunkSize must be >= 1") }

    val session = sessionOrDatabase.fold(session => session, db => new SessionWithAsyncTransaction(db))
    val chunkedFetcher = new ChunkedSlickQueryFetcher(driverProfile, session, query, maybeChunkSize, logCallback)

    Enumerator.generateM(chunkedFetcher.fetchNextChunk) &>
      Enumeratee.onEOF(() => chunkedFetcher.completeTransaction) &>
      Enumeratee.onIterateeDone(() => chunkedFetcher.completeTransaction) &>
      Enumeratee.recover((_, _) => chunkedFetcher.completeTransaction)
  }

  /** Represents a stateful data pump to execute the query in chunks, for use in
    * constructing an Enumerator to represent the response chunks as a stream to
    * be fed to an Iteratee.
    *
    * NOTE: relies on the provided SessionWithAsyncTransaction, as well as the
    *   configuration of the underlying database, to ensure that read consistency
    *   is maintained across the fetching of multiple chunks.
    */
  private class ChunkedSlickQueryFetcher[Q, R](val driverProfile: JdbcProfile,
                                               val session: SessionWithAsyncTransaction,
                                               val query: Query[Q, R],
                                               val maybeChunkSize: Option[Int],
                                               val logCallback: LogCallback)(implicit val ec: ExecutionContext) {
    import driverProfile.Implicit._

    /** Mutable state for this enumeration of query results. Follows the pattern
      * of Enumerator.fromStream, which has an InputStream as mutable state.
      */
    private var position: Int = 0

    /** Returns a Promise containing None when no more results are available */
    def fetchNextChunk: Future[Option[List[R]]] = {
      val startTime = DateTime.now
      val startPosition = position // capture this, since position will change when we execute

      // only places errors might occur: chunking query, generating sql, and executing query
      val futureMaybeChunkedQuery = chunkQuery(query)
      val futureMaybeSql          = futureMaybeChunkedQuery.flatMap(generateSql)
      val futureResults           = futureMaybeChunkedQuery.flatMap(executeQuery)

      // chain async success and error logging onto the above futures
      asyncLogSuccessOrFailure(startTime, startPosition, futureResults, futureMaybeSql)

      futureResults
    }

    /** When done, must call this to ensure that connection is committed, releasing any
      * underlying read locks or other mechanisms used by the database to ensure read
      * consistency across multiple statements in a single transaction.
      */
    def completeTransaction(): Unit = {
      session.ensureAsyncTransactionIsCompleted()
    }

    /** First place that an exception might occur: chunking the query */
    private def chunkQuery(query: Query[Q, R]): Future[Option[Query[Q, R]]] = Future {
      (maybeChunkSize, position) match {
        case (Some(chunkSize), _) => Some(query.drop(position).take(chunkSize))
        case (None, 0)            => Some(query)
        case _                    => None
      }
    }

    /** Second place that an exception might occur: generating the sql (for logging) */
    private def generateSql(maybeQueryWithChunking: Option[Query[Q, R]]): Future[Option[String]] = Future {
      maybeQueryWithChunking.map(_.selectStatement)
    }

    /** Third place that an exception might occur: executing the query */
    private def executeQuery(maybeQueryWithChunking: Option[Query[Q, R]]) = Future {
      if (session.isOpen) {
        val results: List[R] = session.withAsyncTransaction { implicit sessionWithTransaction =>
          maybeQueryWithChunking match {
            case Some(query) => query.list
            case None        => Nil
          }
        }

        position += results.size // update mutable counter based on count of results fetched

        if (results.isEmpty) None else Some(results) // return Future.successful(None) if no results
      } else {
        None
      }
    }

    /** Asynchronously log success or failure as soon as they are available */
    private def asyncLogSuccessOrFailure(startTime: DateTime, startPosition: Int,
                                         futureResults: Future[Option[List[R]]], futureMaybeSql: Future[Option[String]]) {
      // Log any error with sql statement (unless that's where the error occurred, so not available)
      for {
        ex       <- futureResults.failed
        maybeSql <- futureMaybeSql.recover { case _ => None }
        endTime  =  DateTime.now
      } logCallback(LogFields(startTime, endTime, startPosition, None, maybeSql, Some(ex)))

      // Or, log success
      for {
        maybeResults    <- futureResults
        maybeSql        <- futureMaybeSql
        maybeNumResults =  maybeResults.map(_.length)
        endTime         =  DateTime.now
      } logCallback(LogFields(startTime, endTime, startPosition, maybeNumResults, maybeSql, None))
    }

  }

}
