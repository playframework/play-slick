package play.api.db.slick

import play.api._
import scala.slick.driver._

object Config { //I think we should make this private[slick] because I moved the driver to DB.profile and since driver can be misused
  lazy val driver: ExtendedDriver = driver(play.api.Play.current)

  def driver(app: Application): ExtendedDriver = {
    val conf = app.configuration
    val driverKey = getDriverKey(app, conf)
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
        "Slick error : jdbc driver not defined in application.conf for " + app.mode + " mode", None)
    }
  }

  def getDriverKey(app: Application, conf: Configuration) = app.mode match {
    case Mode.Test if(conf.getString("db.test.driver").isDefined) => "db.test.driver"
    //default mode (for dev and prod)
    case _ => "db.default.driver"
  }
}
