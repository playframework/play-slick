import play.api._
import models._
import play.api.db.slick._
import play.api.Play.current
import scala.slick.driver.H2Driver.simple._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.insert()
  }

}

/** Initial set of data to be imported into the sample application. */
object InitialData {

  def insert(): Unit = {
    DB.withSession { implicit s: Session =>
      if (records.length.run == 0) {
        val rows = Seq(
          Record(1, "Alpha"),
          Record(2, "Beta"),
          Record(3, "Gamma"),
          Record(4, "Delta"),
          Record(5, "Epsilon"))

        records.insertAll(rows:_*)
      }
    }
  }

}