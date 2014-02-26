package play.api.db.slick.iteratees

import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.slf4j.{ Logger => Slf4jLogger }
import play.api.Logger

import SlickPlayIteratees.{LogFields, PlayLogCallback}

class PlayLogCallbackUnitSpec extends Specification with Mockito {
  isolated

  val startTime = new DateTime(2013, 11, 19, 17, 8, 3, DateTimeZone.forID("America/New_York"))
  val endTime = startTime.plusMillis(500)
  val fieldsOnSuccess = LogFields(startTime, endTime, 0, Some(100), Some("sql"), None)
  val exception = new RuntimeException("boo!")
  val fieldsOnException = fieldsOnSuccess.copy(maybeNumResults = None, maybeException = Some(exception))

  val slf4jLogger = mock[Slf4jLogger]
  slf4jLogger.isInfoEnabled returns true
  slf4jLogger.isErrorEnabled returns true

  val playLogger = new Logger(slf4jLogger)
  val callback = PlayLogCallback(playLogger)
  val callbackLogSql = PlayLogCallback(playLogger, shouldLogSqlOnSuccess = true)

  "PlayLogCallback" should {

    "on success with default settings" in {
      "logs at info level without SQL" in {
        callback(fieldsOnSuccess)
        there was one(slf4jLogger).info("enumerateSlickQuery - fetched chunk in 500 ms: offset 0, 100 records")
      }

      "logs zero records returned" in {
        callback(fieldsOnSuccess.copy(maybeNumResults = None))
        there was one(slf4jLogger).info("enumerateSlickQuery - fetched chunk in 500 ms: offset 0, 0 records")
      }
    }

    "on success with settings: should log SQL on success" in {
      "logs at info level *with* SQL" in {
        callbackLogSql(fieldsOnSuccess)
        there was one(slf4jLogger).info("enumerateSlickQuery - fetched chunk in 500 ms: offset 0, 100 records [sql]")
      }

      "logs zero records returned" in {
        callbackLogSql(fieldsOnSuccess.copy(maybeNumResults = None))
        there was one(slf4jLogger).info("enumerateSlickQuery - fetched chunk in 500 ms: offset 0, 0 records [sql]")
      }
    }

    "on failure with default settings" in {
      callback(fieldsOnException)

      "logs at error level with SQL" in {
        there was one(slf4jLogger).error("enumerateSlickQuery - failed to fetch chunk in 500 ms: offset 0 [sql]", exception)
      }
    }

    "on failure with settings: should log SQL on success" in {
      callbackLogSql(fieldsOnException)

      "logs at error level with SQL" in {
        there was one(slf4jLogger).error("enumerateSlickQuery - failed to fetch chunk in 500 ms: offset 0 [sql]", exception)
      }
    }

  }

}
