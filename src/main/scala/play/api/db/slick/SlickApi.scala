package play.api.db.slick

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.Try
import scala.util.control.NonFatal

import com.typesafe.config.Config

import javax.inject.Inject
import play.api.Configuration
import play.api.Environment
import play.api.Logger
import play.api.PlayConfig
import play.api.inject.ApplicationLifecycle
import slick.backend.DatabaseConfig
import slick.profile.BasicProfile

trait SlickApi {
  def dbConfigs[P <: BasicProfile](): Seq[(Name, DatabaseConfig[P])]
  def dbConfig[P <: BasicProfile](name: String): DatabaseConfig[P]
}

final class DefaultSlickApi @Inject() (
  environment: Environment,
  configuration: Configuration,
  lifecycle: ApplicationLifecycle) extends SlickApi {

  private val logger = Logger(classOf[DefaultSlickApi])

  // clean-up when the application is stopped.
  lifecycle.addStopHook { () =>
    // FIXME: Should Play modules use the default Play execution context, or should I define a custom one?
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    Future {
      logger.debug("Shutting down databases.")
      // FIXME: This will trigger initialization of the dbs only to shut them down!
      for ((name, dbConfig) <- dbConfigs) {
        val result = Try(dbConfig.db.close())
      }
    }
  }

  private lazy val dbconfigByName: Map[Name, DatabaseConfig[BasicProfile]] = {
    def configs: Map[String, Config] = {
      val config = configuration.underlying
      val slickDbKey = config.getString(SlickModule.DbKeyConfig)
      if (config.hasPath(slickDbKey)) {
        val playConfig = PlayConfig(config)
        playConfig.get[Map[String, Config]](slickDbKey)
      } else Map.empty[String, Config]
    }
    val dbConfigs = collection.mutable.Map.empty[Name, DatabaseConfig[BasicProfile]]
    for ((name, config) <- configs) {
      try {
        val dbConf = slick.backend.DatabaseConfig.forConfig[BasicProfile](path = "", config)
        dbConfigs += Name(name) -> dbConf
      } catch {
        case NonFatal(t) =>
          logger.error(s"Provided bad slick database configuration for $name. Hint: Check your application.conf.", t)
      }
    }
    dbConfigs.toMap
  }

  def dbConfigs[P <: BasicProfile](): Seq[(Name, DatabaseConfig[P])] = {
    val dbs: Seq[(Name, DatabaseConfig[BasicProfile])] = dbconfigByName.toList
    dbs.asInstanceOf[Seq[(Name, DatabaseConfig[P])]]
  }

  def dbConfig[P <: BasicProfile](name: String): DatabaseConfig[P] = {
    // FIXME: Instantiating a single DB triggers initialization of ALL DBs.
    val db: DatabaseConfig[BasicProfile] = dbconfigByName.getOrElse(Name(name), throw new IllegalArgumentException(s"No database configuration exists for $name"))
    db.asInstanceOf[DatabaseConfig[P]]
  }
}