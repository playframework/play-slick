package play.api.db.slick

import play.api._
import play.api.Play.current
import scala.slick.driver._

object Config {
  lazy val driver: ExtendedDriver = Play.application.configuration.getString(driverConfiguration).get match {
    case "org.apache.derby.jdbc.EmbeddedDriver" => DerbyDriver
    case "org.h2.Driver" => H2Driver    
    case "org.hsqldb.jdbcDriver" => HsqldbDriver
    case "com.mysql.jdbc.Driver" => MySQLDriver
    case "org.postgresql.Driver" => PostgresDriver
    case "org.sqlite.JDBC" => SQLiteDriver
    case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerDriver
    case _ => throw new RuntimeException("Slick error : Unknown jdbc driver found in application.conf")
  }

  def driverConfiguration = {
    Play.application.mode match {     
      case Mode.Test if(Play.application.configuration.getString("test.db.default.driver").isDefined) => "test.db.default.driver"
      case Mode.Prod if(Play.application.configuration.getString("prod.db.default.driver").isDefined) => "prod.db.default.driver"
      //default mode
      case _ => "db.default.driver"
    }
  }
}
