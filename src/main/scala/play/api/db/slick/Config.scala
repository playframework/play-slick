package play.api.db.slick

import scala.slick.driver._
import play.api.Application

trait Config {
  val defaultName = "default"

  /** Extend this to add driver or change driver mapping */
  protected def driverByName: String => Option[ExtendedDriver] = Map(
    "org.apache.derby.jdbc.EmbeddedDriver" -> DerbyDriver, "org.h2.Driver" -> H2Driver, "org.hsqldb.jdbcDriver" -> HsqldbDriver, "com.mysql.jdbc.Driver" -> MySQLDriver, "org.postgresql.Driver" -> PostgresDriver, "org.sqlite.JDBC" -> SQLiteDriver, "com.microsoft.sqlserver.jdbc.SQLServerDriver" -> SQLServerDriver, "net.sourceforge.jtds.jdbc.Driver" -> SQLServerDriver).get(_)

  def datasource(name: String, app: Application) = {
    val conf = app.configuration
    conf.getConfig(s"db.$name") match {
      case None => throw conf.reportError(s"db.$name", s"While loading datasource: could not find db.$name in configuration")
      case _ => play.api.db.DB.getDataSource(name)(app)
    }
  }

  def driver(name: String = defaultName)(app: Application)= {
    val conf = app.configuration
    val key = s"db.$name.driver"
    conf.getString(key).map { driverName =>
      driverByName(driverName).getOrElse {
        throw conf.reportError(
          key, s"Slick error : Unknown jdbc driver found in application.conf: [$driverName]")
      }
    }.getOrElse {
      throw conf.reportError(
        key, s"Slick error : jdbc driver not defined in application.conf for db.$name.driver key")
    }
  }
}

object Config extends Config {
  lazy val app = play.api.Play.current
  lazy val driver: ExtendedDriver = driver()(app)
}
