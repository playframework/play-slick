package play.api.db.slick

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.control.NonFatal
import com.typesafe.config.Config
import javax.inject.Inject

import play.api.Configuration
import play.api.Environment
import play.api.Logger
import play.api.PlayException
import play.api.inject.ApplicationLifecycle
import slick.basic.DatabaseConfig
import slick.basic.BasicProfile

trait SlickApi {

  /**
   * Returns all database configs, for all databases defined in the loaded application's configuration.
   *  @throws PlayException if a database config cannot be created.
   */
  @throws(classOf[PlayException])
  def dbConfigs[P <: BasicProfile](): Seq[(DbName, DatabaseConfig[P])]

  /**
   * Returns a database config instance for the database named `name` in the loaded application's configuration.
   * @throws PlayException if a database config for the passed `name` cannot be created.
   */
  @throws(classOf[PlayException])
  def dbConfig[P <: BasicProfile](name: DbName): DatabaseConfig[P]
}

final class DefaultSlickApi @Inject() (
    environment: Environment,
    configuration: Configuration,
    lifecycle: ApplicationLifecycle
)(implicit executionContext: ExecutionContext)
    extends SlickApi {
  import DefaultSlickApi.DatabaseConfigFactory

  private lazy val dbconfigFactoryByName: Map[DbName, DatabaseConfigFactory] = {
    def configs: Map[String, Config] = {
      val config     = configuration.underlying
      val slickDbKey = config.getString(SlickModule.DbKeyConfig)
      if (config.hasPath(slickDbKey)) {
        val playConfig = Configuration(config)
        playConfig.get[Map[String, Config]](slickDbKey)
      } else Map.empty[String, Config]
    }
    (for ((name, config) <- configs) yield (DbName(name), new DatabaseConfigFactory(name, config, lifecycle))).toMap
  }

  // Be careful that accessing this field will trigger initialization of ALL database configs!
  private lazy val allDbConfigs: List[(DbName, DatabaseConfig[BasicProfile])] =
    dbconfigFactoryByName.map { case (name, factory) => (name, factory.get) }.toList

  def dbConfigs[P <: BasicProfile](): Seq[(DbName, DatabaseConfig[P])] =
    allDbConfigs.asInstanceOf[Seq[(DbName, DatabaseConfig[P])]]

  def dbConfig[P <: BasicProfile](name: DbName): DatabaseConfig[P] = {
    val factory: DatabaseConfigFactory = dbconfigFactoryByName.getOrElse(
      name,
      throw new PlayException(s"No database configuration found for ", name.value)
    )
    val dbConf: DatabaseConfig[BasicProfile] = factory.get
    dbConf.asInstanceOf[DatabaseConfig[P]]
  }
}

object DefaultSlickApi {
  private object DatabaseConfigFactory {
    private val logger = Logger(classOf[DefaultSlickApi])
  }
  // This class is useful for delaying the creation of `DatabaseConfig` instances.
  private class DatabaseConfigFactory(name: String, config: Config, lifecycle: ApplicationLifecycle)(
      implicit executionContext: ExecutionContext
  ) {
    import DatabaseConfigFactory.logger

    @throws(classOf[PlayException])
    lazy val get: DatabaseConfig[BasicProfile] = {
      val dbConf = create()
      logger.debug(s"Created Slick database config for key $name.")
      registerDatabaseShutdownHook(dbConf)
      dbConf
    }

    @throws(classOf[PlayException])
    private def create(): DatabaseConfig[BasicProfile] = {
      try DatabaseConfig.forConfig[BasicProfile](path = "", config = config)
      catch {
        case NonFatal(t) =>
          logger.error(s"Failed to create Slick database config for key $name.", t)
          throw Configuration(config).reportError(name, s"Cannot connect to database [$name]", Some(t))
      }
    }

    private def registerDatabaseShutdownHook(dbConf: DatabaseConfig[_]): Unit = {
      // clean-up when the application is stopped.
      lifecycle.addStopHook { () =>
        Future {
          Try(dbConf.db.close()) match {
            case Success(_) => logger.debug(s"Database $name was successfully closed.")
            case Failure(t) => logger.warn(s"Error occurred while closing database $name.", t)
          }
        }
      }
    }
  }
}
