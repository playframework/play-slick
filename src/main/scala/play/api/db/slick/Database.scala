package play.api.db.slick


import play.api.Application
import scala.slick.driver._
import scala.slick.jdbc.PlayDatabase
import java.util.concurrent.ConcurrentHashMap


object Database {
  import collection.JavaConverters._
  private[slick] val cachedDatabases = (new ConcurrentHashMap[Int, Database]()).asScala

  val defaultName = "default"
  def apply(name: String = defaultName)(implicit app: Application) = {
    val id = name.hashCode + app.hashCode
    //creating a new Database means reading the configuration and loading the DataSource,
    //therefore we cache to shave off some millis
    cachedDatabases.getOrElse(id, {
      val db = new Database(name, app)
      cachedDatabases.put(id, db)
      db
    })
  }
}

class Database(val name: String = Database.defaultName, app: Application) extends PlayDatabase {
  def apply(name: String) = Database(name)(app)

  def apply(app: Application) = Database(name)(app)

  val conf = app.configuration

  protected def datasource = {
    import conf._
    getConfig(s"db.$name") match {
      case None => throw reportError(s"db.$name", s"While loading datasource: could not find db.$name in configuration")
      case _ => play.api.db.DB.getDataSource(name)(app)
    }
  }

  private def driverByName: String => Option[JdbcDriver] = Map(
    "org.apache.derby.jdbc.EmbeddedDriver" -> DerbyDriver, "org.h2.Driver" -> H2Driver, 
    "org.hsqldb.jdbcDriver" -> HsqldbDriver, "com.mysql.jdbc.Driver" -> MySQLDriver, 
    "org.postgresql.Driver" -> PostgresDriver, "org.sqlite.JDBC" -> SQLiteDriver, 
    "com.microsoft.sqlserver.jdbc.SQLServerDriver" -> SQLServerDriver, 
    "net.sourceforge.jtds.jdbc.Driver" -> SQLServerDriver).get(_)
  def driver = {
    val key = s"db.$name.driver"
    import conf._
    getString(key).map { driverName =>
      driverByName(driverName).getOrElse {
        throw reportError(
          key, s"Slick error : Unknown jdbc driver found in application.conf: [$driverName]")
      }
    }.getOrElse {
      throw reportError(
        key, s"Slick error : jdbc driver not defined in application.conf for db.$name.driver key")
    }
  }
}
