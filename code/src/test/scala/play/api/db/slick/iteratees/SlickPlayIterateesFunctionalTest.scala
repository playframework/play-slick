package play.api.db.slick.iteratees

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Random}

import com.typesafe.slick.testkit.util.InternalJdbcTestDB
import java.sql.DriverManager
import org.h2.jdbc.JdbcSQLException
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import play.api.libs.iteratee.{Enumeratee, Error, Input, Iteratee}
import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.SessionWithAsyncTransaction

import SlickPlayIteratees.{LogFields, LogCallback, enumerateSlickQuery}


class SlickPlayIterateesFunctionalTest extends Specification with NoTimeConversions {

  // Only one test can execute at a time, as they share an in-memory H2 database
  sequential

  // Force H2 driver registration, to fix "java.sql.SQLException: No suitable driver found".
  // Probably caused by overeager driver deregistration in a previous test. It may be
  // possible to remove this line and the line in TestableDBActionTest in the near future
  // once a DB plugin fix makes it into Play: https://github.com/playframework/playframework/pull/2794
  DriverManager.registerDriver(new org.h2.Driver())

  // Create in-memory test DB and import its implicits
  val tdb = new InternalJdbcTestDB("h2mem") {
    val url = "jdbc:h2:mem:slick-play-iteratees_spec;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;LOCK_MODE=1;MVCC=FALSE"
    val jdbcDriver = "org.h2.Driver"
    val driver = scala.slick.driver.H2Driver
  }

  lazy val db = tdb.createDB()
  import tdb.driver.simple._

  // names for certain sets of test rows
  val fiveRowsInDb = List((1, "a"), (2, "b"), (3, "c"), (4, "d"), (5, "e")).map(TestRow.tupled)
  val twoRowsInDb = fiveRowsInDb.take(2)
  val rowsInDbExcludingC = fiveRowsInDb.filterNot(_.name == "c")

  ".enumerateSlickQuery" should {

    "Basic happy path" in {

      "should enumerate query results in 0 chunks when chunkSize = 2 and query has 0 results" in {
        testChunkedEnumerationUsingInMemoryDb(Nil, Some(2), Nil)
      }

      "should enumerate query results in 1 chunk when chunkSize = 2 and query has 2 results" in {
        testChunkedEnumerationUsingInMemoryDb(twoRowsInDb, Some(2), List(twoRowsInDb))
      }

      "should enumerate query results in 3 chunks when chunkSize = 2 and query has 5 results" in {
        testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, Some(2), fiveRowsInDb.grouped(2).toList)
      }

      "should enumerate query results in 2 chunks when chunkSize = 2 and query has 4 results after applying criteria" in {
        val criterion: TestQueryCriterion = _.name =!= "c"
        testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, Some(2), rowsInDbExcludingC.grouped(2).toList, Some(rowsInDbExcludingC), Seq(criterion))
      }

      "should enumerate query results in 1 chunk when chunkSize = 10 and query has 5 results" in {
        testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, Some(10), List(fiveRowsInDb))
      }

      "should enumerate query results in 1 chunk when no chunkSize and query has 5 results" in {
        testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, None, List(fiveRowsInDb))
      }

      "should close transaction after successful execution" in {
        val session = new SessionWithAsyncTransaction(db)
        testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, None, List(fiveRowsInDb), maybeExternalSession = Some(session))
        session.isInTransaction must beFalse
      }

      "should close underlying jdbc connection after successful execution" in {
        val session = new SessionWithAsyncTransaction(db)
        testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, None, List(fiveRowsInDb), maybeExternalSession = Some(session))
        session.isOpen must beFalse
      }

    }

    "Error handling" in {

      "should throw argument exception if chunkSize <= 0" in {
        testChunkedEnumerationUsingInMemoryDb(Nil, Some(0), Nil) must throwAn [IllegalArgumentException]
      }

      "should propagate exception generated during query execution" in {
        val criterion: TestQueryCriterion = (_.doesNotExist isDefined)
        testChunkedEnumerationUsingInMemoryDb(Nil, None, Nil, criteria = Seq(criterion)) must throwA [JdbcSQLException].like {
          case e => e.getMessage must beMatching("""(?s)Column "\w+\.DOES_NOT_EXIST" not found;.*""")
        }
      }

      "should close transaction when exception generated during query execution" in {
        val session = new SessionWithAsyncTransaction(db)
        val criterion: TestQueryCriterion = (_.doesNotExist isDefined)
        testChunkedEnumerationUsingInMemoryDb(Nil, None, Nil, criteria = Seq(criterion), maybeExternalSession = Some(session)) must throwA [JdbcSQLException]
        session.isInTransaction must beFalse
      }

      "should close underlying jdbc connection when exception generated during query execution" in {
        val session = new SessionWithAsyncTransaction(db)
        val criterion: TestQueryCriterion = (_.doesNotExist isDefined)
        testChunkedEnumerationUsingInMemoryDb(Nil, None, Nil, criteria = Seq(criterion), maybeExternalSession = Some(session)) must throwA [JdbcSQLException]
        session.isOpen must beFalse
      }

      "should close transaction when exception generated in downstream Enumeratee" in {
        val session = new SessionWithAsyncTransaction(db)
        val exceptionThrowingEnumeratee = Enumeratee.map { chunk: List[TestRow] => throw new RuntimeException("boo!"); chunk }
        Try {
          testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, Some(2), rowsInDbExcludingC.grouped(2).toList,
            maybeExtraEnumeratee = Some(exceptionThrowingEnumeratee),
            maybeExternalSession = Some(session))
        }
        session.isInTransaction must beFalse
      }

      "should close underlying jdbc connection when exception generated in downstream Enumeratee" in {
        val session = new SessionWithAsyncTransaction(db)
        val exceptionThrowingEnumeratee = Enumeratee.map { chunk: List[TestRow] => throw new RuntimeException("boo!"); chunk }
        Try {
          testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, Some(2), rowsInDbExcludingC.grouped(2).toList,
            maybeExtraEnumeratee = Some(exceptionThrowingEnumeratee),
            maybeExternalSession = Some(session))
        }
        session.isOpen must beFalse
      }

      "should close transaction when downstream Enumeratee is in Error state" in {
        val session = new SessionWithAsyncTransaction(db)
        val errorStateEnumeratee = new Enumeratee[List[TestRow], List[TestRow]] {
          def applyOn[A](inner: Iteratee[List[TestRow], A]) = Error("testing!", Input.Empty)
        }
        testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, Some(2), rowsInDbExcludingC.grouped(2).toList,
          maybeExtraEnumeratee = Some(errorStateEnumeratee),
          maybeExternalSession = Some(session)) must throwA [RuntimeException]
        session.isInTransaction must beFalse
      }

      "should close underlying jdbc connection when downstream Enumeratee is in Error state" in {
        val session = new SessionWithAsyncTransaction(db)
        val errorStateEnumeratee = new Enumeratee[List[TestRow], List[TestRow]] {
          def applyOn[A](inner: Iteratee[List[TestRow], A]) = Error("testing!", Input.Empty)
        }
        testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, Some(2), rowsInDbExcludingC.grouped(2).toList,
          maybeExtraEnumeratee = Some(errorStateEnumeratee),
          maybeExternalSession = Some(session)) must throwA [RuntimeException]
        session.isOpen must beFalse
      }

    }

    "Read consistency (transactions)" in {

      "should provide consistent reads *by default* when writes are interleaved" in {
        testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, Some(2), fiveRowsInDb.grouped(2).toList,
          maybeExtraEnumeratee = Some(createInterleavedWritesEnumeratee),
          maybeExternalSession = None)
      }

      "should provide consistent reads given a real external session with async transaction" in {
        testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, Some(2), fiveRowsInDb.grouped(2).toList,
          maybeExtraEnumeratee = Some(createInterleavedWritesEnumeratee),
          maybeExternalSession = Some(new SessionWithAsyncTransaction(db)))
      }

      "[test of preceding tests] should fail to provide consistent reads given a *fake* external session with async transaction" in {
        val r = Try {
          testChunkedEnumerationUsingInMemoryDb(fiveRowsInDb, Some(2), fiveRowsInDb.grouped(2).toList,
            maybeExtraEnumeratee = Some(createInterleavedWritesEnumeratee),
            maybeExternalSession = Some(new FakeSessionWithAsyncTransactionForTesting(db)))
        }

        r.isFailure must beTrue
        r.failed.map(_.getMessage).get must contain ("is not equal to")
      }

    }

    "Logging callbacks" in {

      "when fetch is successful" in {
        "should log startTime and endTime" in { testLoggedStartAndEndTimes(scenario = Success) }
        "should log sql statement"         in { testLoggedSqlStatement(scenario = Success) }
        "should *not* log an exception"    in { testLoggedNoException(scenario = Success) }
      }

      "when fetch throws an exception" in {
        "should log startTime and endTime" in { testLoggedStartAndEndTimes(scenario = ThrowInFetch) }
        "should log sql statement"         in { testLoggedSqlStatement(scenario = ThrowInFetch) }
        "should log the exception"         in { testLoggedException(scenario = ThrowInFetch) }
      }

      "when sql generation throws an exception" in {
        "should log startTime and endTime" in { testLoggedStartAndEndTimes(scenario = ThrowInSqlGen) }
        "should *not* log sql statement"   in { testLoggedNoSqlStatement(scenario = ThrowInSqlGen) }
        "should log the exception"         in { testLoggedException(scenario = ThrowInSqlGen, exceptionClass = classOf[TestSqlGenException]) }
      }

    }

  }

  case class TestRow(id: Int, name: String)

  // Real schema, only including the real columns to create in the db
  class TestTableRealSchema(tag: Tag) extends Table[TestRow](tag, "TEST") {
    def id = column[Int]("ID")
    def name = column[String]("NAME")
    def * = (id, name) <> (TestRow.tupled, TestRow.unapply)
  }
  lazy val testRows = TableQuery[TestTableRealSchema]

  // For testing, this table definition includes and extra column NOT in the db
  class TestTableWithMissingColumn(tag: Tag) extends Table[TestRow](tag, "TEST") {
    def id = column[Int]("ID")
    def name = column[String]("NAME")
    def doesNotExist = column[Option[String]]("DOES_NOT_EXIST") // this NamedColumn will not be in SQL schema above
    def * = (id, name) <> (TestRow.tupled, TestRow.unapply)
  }
  lazy val testRowsNoCol = TableQuery[TestTableWithMissingColumn]

  type TestQueryCriterion = (TestTableWithMissingColumn) => Column[Boolean]

  def testChunkedEnumerationUsingInMemoryDb(rowsInDb: List[TestRow],
                                            maybeChunkSize: Option[Int],
                                            expectedChunksSent: List[List[TestRow]],
                                            maybeExpectedResults: Option[List[TestRow]] = None,
                                            criteria: Seq[TestQueryCriterion] = Nil,
                                            maybeExtraEnumeratee: Option[Enumeratee[List[TestRow], List[TestRow]]] = None,
                                            maybeExternalSession: Option[SessionWithAsyncTransaction] = None,
                                            maybeDriverProfile: Option[JdbcProfile] = None,
                                            logCallback: LogCallback = _ => ()) = {
    // Create table if not exist, and insert rows
    db withSession { implicit session =>
      try {
        testRows.ddl.drop
      }
      catch { case ex: JdbcSQLException if ex.getMessage.contains("Table \"TEST\" not found") => /* ignore */ }
      testRows.ddl.create
      testRows.insertAll(rowsInDb:_*)
    }

    // Query the table
    val baseQuery = for { test <- testRowsNoCol } yield test
    baseQuery.selectStatement
    def mkQuery(criteria: TestQueryCriterion*) = criteria.foldLeft(baseQuery) { (q, c) => q.filter(c) }

    // Enumerate in chunks, through an Enumeratee which records each chunk as an effect, and an
    //   optional extra Enumeratee passed in by the caller, into a consuming Iteratee
    val sessionOrDatabase = maybeExternalSession.toLeft(db)
    val driver = maybeDriverProfile.getOrElse(tdb.driver)
    val enumerator = enumerateSlickQuery(driver, sessionOrDatabase, mkQuery(criteria:_*), maybeChunkSize, logCallback)

    var chunksSent: List[List[TestRow]] = Nil
    val chunksSentEnumeratee = Enumeratee.map { chunk: List[TestRow] => chunksSent = chunksSent :+ chunk; chunk }
    val identityEnumeratee = Enumeratee.map { chunk: List[TestRow] => chunk }
    val extraEnumeratee = maybeExtraEnumeratee.getOrElse(identityEnumeratee)

    val iteratee = Iteratee.consume[List[TestRow]]()

    // Run the Enumerator -> Enumeratee(s) -> Iteratee pipeline
    val eventuallyResult = (enumerator &> chunksSentEnumeratee &> extraEnumeratee |>> iteratee).flatMap(_.run)
    val result = Await.result(eventuallyResult, 5.seconds)

    // Verify expectations
    result must be_==(maybeExpectedResults.getOrElse(rowsInDb)) // returned expected rows
    chunksSent must be_==(expectedChunksSent)                   // chunked as expected
  }

  def createInterleavedWritesEnumeratee: Enumeratee[List[TestRow], List[TestRow]] = {
    // mutable state which will be closed over by the function
    var numRowsLeftToAdd = 2

    // asynchronous effect: adds a random row, and mutates above counter
    // NOTE (2013-10-30, msiegel): made this async to handle databases (such as H2),
    //   whose "consistent reads" mode causes the writes to be blocked by locks until
    //   the read completes
    def addNewRowAsync(): Unit = {
      if (numRowsLeftToAdd > 0) {
        Future { addNewRow() }
        Thread.sleep(500) // give it a chance to try (makes the test fail before the fix)
        numRowsLeftToAdd -= 1
      }
    }

    def addNewRow(): Unit = {
      db withSession { implicit session =>
        testRows.insert(TestRow(Random.nextInt(), Random.alphanumeric.take(5).mkString))
      }
    }

    // return an enumeratee which calls the above function on each chunk
    Enumeratee.map { chunk: List[TestRow] => addNewRowAsync(); chunk }
  }

  /** Fulfills this interface, but uses only local sessions for each block */
  class FakeSessionWithAsyncTransactionForTesting(db: Database) extends SessionWithAsyncTransaction(db) {
    override def withAsyncTransaction[T](f: (Session) => T) = db.withSession(f)
  }

  sealed trait TestScenariosForLogging
  case object Success extends TestScenariosForLogging
  case object ThrowInFetch extends TestScenariosForLogging
  case object ThrowInSqlGen extends TestScenariosForLogging

  def testLoggedStartAndEndTimes(scenario: TestScenariosForLogging) = {
    val logged = testLogging(scenario)
    logged.startTime.getMillis should be <= (logged.endTime.getMillis)
  }

  def testLoggedNoSqlStatement(scenario: TestScenariosForLogging) = {
    testLogging(scenario).maybeSqlStmt must be(None)
  }

  def testLoggedSqlStatement(scenario: TestScenariosForLogging) = {
    testLogging(scenario).maybeSqlStmt must beSome.which(_.contains("limit"))
  }

  def testLoggedNoException(scenario: TestScenariosForLogging) = {
    testLogging(scenario).maybeException must be(None)
  }

  def testLoggedException(scenario: TestScenariosForLogging, exceptionClass: Class[_] = classOf[JdbcSQLException]) = {
    val maybeException = testLogging(scenario).maybeException
    maybeException must beSome.which(_.getClass == exceptionClass)
  }

  def testLogging(scenario: TestScenariosForLogging): LogFields = {
    val promisedLogged = Promise[LogFields]()

    Try {
      val throwingCriterion: TestQueryCriterion = (_.doesNotExist isDefined)

      val criteria = scenario match {
        case ThrowInFetch => Seq(throwingCriterion)
        case _            => Nil
      }

      val maybeExtendedProfile = scenario match {
        case ThrowInSqlGen => Some(new scala.slick.driver.H2Driver {
          override def createQueryBuilder(n: scala.slick.ast.Node, state: scala.slick.compiler.CompilerState): QueryBuilder = { throw new TestSqlGenException() }
        })
        case _ => None
      }

      testChunkedEnumerationUsingInMemoryDb(
        fiveRowsInDb, Some(2), fiveRowsInDb.grouped(2).toList,
        criteria = criteria,
        maybeDriverProfile = maybeExtendedProfile,
        logCallback = f => { promisedLogged.trySuccess(f) })
    }

    Await.result(promisedLogged.future, 1 second)
  }

  class TestSqlGenException extends RuntimeException

}
