package play.api.db.slick

import scala.slick.driver._
import play.api.{Configuration, Application}

trait Config {
  val defaultName = "default"

  /** Extend this to add driver or change driver mapping */
  protected def driverByName: String => Option[JdbcDriver] = Map(
    "org.apache.derby.jdbc.EmbeddedDriver" -> DerbyDriver, "org.h2.Driver" -> H2Driver, "org.hsqldb.jdbcDriver" -> HsqldbDriver, "com.mysql.jdbc.Driver" -> MySQLDriver, "org.postgresql.Driver" -> PostgresDriver, "org.sqlite.JDBC" -> SQLiteDriver).get(_)

  def datasource(name: String, app: Application) = {
    val conf = app.configuration
    conf.getConfig(s"db.$name") match {
      case None => throw conf.reportError(s"db.$name", s"While loading datasource: could not find db.$name in configuration")
      case _ => play.api.db.DB.getDataSource(name)(app)
    }
  }

  private def classExists(name: String): Boolean = try{
    Class.forName(name)
    true
  }catch{
    case e: ClassNotFoundException =>
      false
  }

  private def arbitraryDriver(name: String)(conf: Configuration, key: String): JdbcDriver = {
    val clazz = try{
      Class.forName(name+"$")
    }catch{
      case e: ClassNotFoundException if(classExists(name)) =>
        throw conf.reportError(key, s"The class $name is not an object. Use 'object' keyword, please. If it is a third-party class, you may want to create a subclass.")
    }
    val instanceField = clazz.getField("MODULE$")
    instanceField.get() match{
      case driver: JdbcDriver => driver
      case _ => throw conf.reportError(key, s"The class $name is not a "+classOf[JdbcDriver].getName+".")
    }
  }

  def driver(name: String = defaultName)(app: Application)= {
    val conf = app.configuration
    val key = s"db.$name.driver"
    conf.getString(key).map { driverName =>
      def arbitraryDriverOption = conf.getString(s"db.$name.slickdriver").map(arbitraryDriver(_)(conf, key))
      arbitraryDriverOption.orElse(driverByName(driverName)).getOrElse {
        throw conf.reportError(
          key, s"Slick error : Unknown jdbc driver found in application.conf: [$driverName]. If you have a Slick driver for this database, you can put its class name to db.$name.slickdriver.")
      }
    }.getOrElse {
      throw conf.reportError(
        key, s"Slick error : jdbc driver not defined in application.conf for db.$name.driver key")
    }
  }
}

object Config extends Config {
  lazy val app = play.api.Play.current
  lazy val driver: JdbcDriver = driver()(app)
}
