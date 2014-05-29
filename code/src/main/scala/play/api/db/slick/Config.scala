package play.api.db.slick

import javax.sql.DataSource
import scala.slick.driver._
import play.api.{ Configuration, Application }

trait Config {
  val defaultName = "default"

  /** Extend this to add driver or change driver mapping */
  protected def driverByName: String => Option[JdbcDriver] = Map(
    "org.apache.derby.jdbc.EmbeddedDriver" -> DerbyDriver, 
    "org.h2.Driver" -> H2Driver, 
    "org.hsqldb.jdbcDriver" -> HsqldbDriver, 
    "org.hsqldb.jdbc.JDBCDriver" -> HsqldbDriver, 
    "com.mysql.jdbc.Driver" -> MySQLDriver, 
    "org.postgresql.Driver" -> PostgresDriver, 
    "org.sqlite.JDBC" -> SQLiteDriver).get(_)


  def datasource(name: String, app: Application): DataSource = {
    val conf = app.configuration
    conf.getConfig(s"db.$name") match {
      case None => throw conf.reportError(s"db.$name", s"While loading datasource: could not find db.$name in configuration")
      case _ => play.api.db.DB.getDataSource(name)(app)
    }
  }

  private def classExists(name: String, classLoader: ClassLoader): Boolean =
    try {
      Class.forName(name, true, classLoader)
      true
    } catch {
      case e: ClassNotFoundException =>
        false
    }

  private def findClass(conf: Configuration, key: String, name: String, classLoader: ClassLoader): Class[_] =
    try {
      Class.forName(name + "$", true, classLoader)
    } catch {
      case e: ClassNotFoundException if (classExists(name, classLoader)) =>
        throw conf.reportError(key, s"The class $name is not an object. Use 'object' keyword, please. If it is a third-party class, you may want to create a subclass.")
    }

  private def arbitraryDriver(name: String)(app: Application, conf: Configuration, key: String): JdbcDriver = {
    import scala.language.existentials
    val clazz = try {
      findClass(conf, key, name, this.getClass().getClassLoader())
    } catch {
      case e: ClassNotFoundException => findClass(conf, key, name, app.classloader)
    }
    val instanceField = clazz.getField("MODULE$")
    instanceField.get() match {
      case driver: JdbcDriver => driver
      case _                  => throw conf.reportError(key, s"The class $name is not a ${classOf[JdbcDriver].getName}.")
    }
  }

  def driver(name: String = defaultName)(app: Application): JdbcDriver = {
    val conf = app.configuration
    val key = s"db.$name.driver"
    val driverName = conf.getString(key).getOrElse {
      throw conf.reportError(
        key, s"Slick error : jdbc driver not defined in application.conf for $key key")
    }
    conf.getString(s"db.$name.slickdriver").map(arbitraryDriver(_)(app, conf, key))
      .orElse(driverByName(driverName))
      .getOrElse {
        throw conf.reportError(
          key, s"Slick error : Unknown jdbc driver found in application.conf: [$driverName]. If you have a Slick driver for this database, you can put its class name to db.$name.slickdriver.")
      }

  }
}

object Config extends Config {
  def app = play.api.Play.current
  lazy val driver: JdbcDriver = driver()(app)
}
