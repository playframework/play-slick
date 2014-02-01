package play.api.db.slick


import play.api.Application
import scala.slick.driver._
import scala.slick.jdbc.PlayDatabase
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource


object Database {
  import collection.JavaConverters._
  private[slick] val cachedDatabases = (new ConcurrentHashMap[Int, Database]()).asScala

  def apply(name: String = Config.defaultName)(implicit app: Application) = {
    val id = name.hashCode + app.hashCode
    //creating a new Database means reading the configuration and loading the DataSource,
    //therefore we cache to shave off some millis
    cachedDatabases.getOrElse(id, {
      val db = new Database(name, Config.datasource(name, app), Config.driver(name)(app))
      cachedDatabases.put(id, db)
      db
    })
  }
}

class Database(val name: String = Config.defaultName, override val datasource: DataSource, val driver: JdbcDriver) extends PlayDatabase
