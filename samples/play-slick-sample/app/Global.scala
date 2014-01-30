import java.text.SimpleDateFormat
import play.api._
import models._
import play.api.db.slick._
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    import play.api.db.slick.Config.driver.simple._

    val ddl = TableQuery[Cats].ddl

    DB.withSession { implicit s =>
      try {
        ddl.create
      } catch {
        case e: Exception =>
          play.api.Logger.warn("While creating tables got: " + e.getCause)
        //FIXME: this is ugly, but it is here in case tables are already created
      }
    }
  }
}