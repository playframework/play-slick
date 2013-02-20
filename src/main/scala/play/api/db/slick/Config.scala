package play.api.db.slick

import play.api._
import play.api.Play.current
import scala.slick.driver._

object Config {
  val conf = Play.application.configuration
  lazy val driver: ExtendedDriver = driverConfiguration(conf) match {
    case "org.apache.derby.jdbc.EmbeddedDriver" => DerbyDriver
    case "org.h2.Driver" => H2Driver    
    case "org.hsqldb.jdbcDriver" => HsqldbDriver
    case "com.mysql.jdbc.Driver" => MySQLDriver
    case "org.postgresql.Driver" => PostgresDriver
    case "org.sqlite.JDBC" => SQLiteDriver
    case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerDriver
    case _ => throw conf.reportError("slick jdbc driver", "Slick error : Unknown jdbc driver found in application.conf: [" + driver + "]", None)
  }

  def driverConfiguration(conf: Configuration) = {
    val driverConfKey = Play.application.mode match {     
      case Mode.Test if(conf.getString("db.test.driver").isDefined) => "db.test.driver"
      //default mode (for dev and prod)
      case _ => "db.default.driver"
    }
    val driverConfValue = conf.getString(driverConfKey)
    if (driverConfValue.isDefined) driverConfValue.get 
    else throw conf.reportError("slick jdbc driver", "Slick error : jdbc driver not defined in application.conf for " + Play.application.mode + "mode", None)
  }
}
