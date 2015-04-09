import scala.concurrent.Await
import scala.concurrent.duration.Duration

import dao.RecordsDAO
import models.Record
import play.api.Application
import play.api.GlobalSettings

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.insert()
  }

}

/** Initial set of data to be imported into the sample application. */
object InitialData {

  def recordsDao = new RecordsDAO
  def insert(): Unit = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    val storedRecords = Await.result(recordsDao.count(), Duration.Inf)
    if (storedRecords == 0) {
      val rows = Seq(
        Record(1, "Alpha"),
        Record(2, "Beta"),
        Record(3, "Gamma"),
        Record(4, "Delta"),
        Record(5, "Epsilon"))

      Await.result(recordsDao.insert(rows), Duration.Inf)
    }
  }
}