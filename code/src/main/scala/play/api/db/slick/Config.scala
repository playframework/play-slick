package play.api.db.slick

import javax.inject.{ Inject, Singleton }
import javax.sql.DataSource
import play.api.{ Application, Configuration, Environment }
import play.api.db.DBApi
import scala.slick.driver._

trait SlickConfig {
  def datasource(name: String): DataSource
  def driver(name: String): JdbcDriver
  def packageNames: Map[String, Set[String]]
  def error(key: String, e: Throwable): Nothing
}

@Singleton
class DefaultSlickConfig @Inject() (conf: Configuration, environment: Environment, dbApi: DBApi) extends SlickConfig {

  val packageNames: Map[String, Set[String]] = {
    (for {
      config <- conf.getConfig("slick").toSeq
      key <- config.keys
      names = config.getString(key).getOrElse(keyNotFound(key)).split(",").toSet
    } yield {
      key -> names
    }).toMap
  }

  private def keyNotFound(key: String): Nothing =
    throw conf.reportError(key, "Expected key " + key + " but could not get its values!")

  /** Extend this to add driver or change driver mapping */
  protected def driverByName: String => Option[JdbcDriver] = Map(
    "org.apache.derby.jdbc.EmbeddedDriver" -> DerbyDriver,
    "org.h2.Driver" -> H2Driver,
    "org.hsqldb.jdbcDriver" -> HsqldbDriver,
    "org.hsqldb.jdbc.JDBCDriver" -> HsqldbDriver,
    "com.mysql.jdbc.Driver" -> MySQLDriver,
    "org.postgresql.Driver" -> PostgresDriver,
    "org.sqlite.JDBC" -> SQLiteDriver).get(_)


  def datasource(name: String): DataSource = {
    conf.getConfig(s"db.$name") match {
      case None => throw conf.reportError(s"db.$name", s"While loading datasource: could not find db.$name in configuration")
      case _ => dbApi.database(name).dataSource
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

  private def findClass(key: String, name: String, classLoader: ClassLoader): Class[_] =
    try {
      Class.forName(name + "$", true, classLoader)
    } catch {
      case e: ClassNotFoundException if (classExists(name, classLoader)) =>
        throw conf.reportError(key, s"The class $name is not an object. Use 'object' keyword, please. If it is a third-party class, you may want to create a subclass.")
    }

  private def arbitraryDriver(key: String)(name: String): JdbcDriver = {
    val clazz: Class[_] = try {
      findClass(key, name, this.getClass().getClassLoader())
    } catch {
      case e: ClassNotFoundException => findClass(key, name, environment.classLoader)
    }
    val instanceField = clazz.getField("MODULE$")
    instanceField.get(null) match {
      case driver: JdbcDriver => driver
      case _                  => throw conf.reportError(key, s"The class $name is not a ${classOf[JdbcDriver].getName}.")
    }
  }

  def driver(name: String = Config.defaultName): JdbcDriver = {
    val key = s"db.$name.driver"
    val driverName = conf.getString(key).getOrElse {
      throw conf.reportError(
        key, s"Slick error : jdbc driver not defined in application.conf for $key key")
    }
    conf.getString(s"db.$name.slickdriver").map(arbitraryDriver(key))
      .orElse(driverByName(driverName))
      .getOrElse {
        throw conf.reportError(
          key, s"Slick error : Unknown jdbc driver found in application.conf: [$driverName]. If you have a Slick driver for this database, you can put its class name to db.$name.slickdriver.")
      }
  }

  def error(key: String, e: Throwable): Nothing =
    throw conf.reportError(key, e.getMessage, Some(e))
}

object Config {
  val defaultName = "default"

  // provide current SlickConfig in place of play.api.Play.current
  // to allow the global driver to be used for evolutions
  @volatile private var current: Option[SlickConfig] = None

  def set(config: SlickConfig): Unit = current = Some(config)

  def get(): SlickConfig = current.getOrElse(sys.error("There is no current SlickConfig"))

  lazy val driver: JdbcDriver = get.driver(defaultName)

  def datasource(name: String, app: Application): DataSource = {
    val config = app.injector.instanceOf[SlickConfig]
    config.datasource(name)
  }

  def driver(name: String = defaultName)(app: Application): JdbcDriver = {
    val config = app.injector.instanceOf[SlickConfig]
    config.driver(name)
  }
}
