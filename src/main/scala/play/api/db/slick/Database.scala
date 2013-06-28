package play.api.db.slick
import scala.slick.session.PlayDatabase
import play.api.Application
import scala.slick.driver._

case class Database(name:String = "default")(implicit app: Application) extends PlayDatabase{
  def apply(name:String)     = Database(name)(app)
  def apply(app:Application) = Database(name)(app)
  val conf = app.configuration
  protected def datasource = {
    import conf._
    getConfig(s"db.$name") match{
      case None => throw reportError(s"db.$name", s"While loading datasource: could not find db.$name in configuration")
      case _ => play.api.db.DB.getDataSource(name)(app)
    }
  }
  private def driverByName : String => Option[ExtendedDriver] = Map(
     "org.apache.derby.jdbc.EmbeddedDriver" -> DerbyDriver
     ,"org.h2.Driver" -> H2Driver
     ,"org.hsqldb.jdbcDriver" -> HsqldbDriver
     ,"com.mysql.jdbc.Driver" -> MySQLDriver
     ,"org.postgresql.Driver" -> PostgresDriver
     ,"org.sqlite.JDBC" -> SQLiteDriver
     ,"com.microsoft.sqlserver.jdbc.SQLServerDriver" -> SQLServerDriver
  ).get(_)
  def driver = {
    val key = s"db.$name.driver"
    import conf._
    getString(key).map{ driverName =>
      driverByName(driverName).getOrElse{
        throw reportError(
          key, s"Slick error : Unknown jdbc driver found in application.conf: [$driverName]"
        )
      }
    }.getOrElse{
      throw reportError( 
        key, s"Slick error : jdbc driver not defined in application.conf for db.$name.driver key"
      )
    }
  }
}
