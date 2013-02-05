package play.api.db.slick

import play.api._
import play.api.Play.current
import scala.slick.driver._

object Store {
  lazy val driver: ExtendedDriver = Play.application.configuration.getString(driverConfiguration).get match {
    case "org.apache.derby.jdbc.EmbeddedDriver" => DerbyDriver
    case "org.h2.Driver" => H2Driver    
    case "org.hsqldb.jdbcDriver" => HsqldbDriver
    case "com.mysql.Driver" => MySQLDriver
    case "org.postgresql.Driver" => PostgresDriver
    case "org.sqlite.JDBC" => SQLiteDriver
    case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => SQLServerDriver
    //more common driver?
    case _ => MySQLDriver
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