import play.api._
import play.api.db.slick._
import play.api.Play.current
import scala.slick.driver.H2Driver.simple._

import models._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.insert()
  }

}

/** Initial set of data to be imported into the sample application. */
object InitialData {

  def insert(): Unit = {
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    DB.withSession { implicit s: Session =>
      if (Records.DAO().count == 0) {
        val rows = Seq(
          Record(1, "Alpha"),
          Record(2, "Beta"),
          Record(3, "Gamma"),
          Record(4, "Delta"),
          Record(5, "Epsilon"))

        Records.records.insertAll(rows:_*)
      }
    }
  }

}