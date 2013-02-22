package play.api.db.slick

import play.api._
import scala.slick.driver._

object Config {
  lazy val driver: ExtendedDriver = driver(play.api.Play.current)

  def driver(app: Application): ExtendedDriver = {
    val conf = app.configuration
    val driverKey = "db.default.driver"
    conf.getString(driverKey) match {
      case Some(driver) => driver  match {
        case "org.apache.derby.jdbc.EmbeddedDriver" => DerbyDriver
        case "org.h2.Driver" => H2Driver
        case "org.hsqldb.jdbcDriver" => HsqldbDriver
        case "com.mysql.jdbc.Driver" => MySQLDriver
        case "org.postgresql.Driver" => PostgresDriver
        case "org.sqlite.JDBC" => SQLiteDriver
        case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerDriver
        case _ => throw conf.reportError(driverKey,
          "Slick error : Unknown jdbc driver found in application.conf: [" + driver + "]", None)
      }
      case None => throw conf.reportError(driverKey,
        "Slick error : jdbc driver not defined in application.conf for db.default.driver key", None)
    }
  }
  
}
