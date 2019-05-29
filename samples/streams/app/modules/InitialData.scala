package modules

import javax.inject.Inject

import com.google.inject.AbstractModule
import dao.RecordsDAO
import models.Record
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

class InitialDataModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[InitialData]).asEagerSingleton()
  }
}

/** Initial set of data to be imported into the sample application. */
class InitialData @Inject() (recordsDAO: RecordsDAO)(implicit executionContext: ExecutionContext) {

  private val logger = LoggerFactory.getLogger(classOf[InitialData])

  Await.result(
    recordsDAO.count().flatMap { count =>
      logger.debug("Initializing data")
      if (count == 0) {
        logger.debug("No data found. Need to insert some records")
        val rows = Seq(
          Record(1, "Alpha"),
          Record(2, "Beta"),
          Record(3, "Gamma"),
          Record(4, "Delta"),
          Record(5, "Epsilon"))
        recordsDAO.insert(rows)
      } else {
        logger.debug("Already found some data. No need to insert any records")
        Future.successful(Option(0))
      } // zero records inserted
    },
    Duration.Inf)
}