package play.api.db.slick

import scala.collection.immutable.Seq
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

import play.api.Configuration
import play.api.Environment
import play.api.Mode
import play.api.PlayException
import play.api.inject.ApplicationLifecycle
import play.api.inject.Binding
import play.api.inject.BindingKey
import play.api.inject.Module
import play.api.libs.Files
import play.db.NamedDatabaseImpl
import slick.basic.DatabaseConfig
import slick.basic.BasicProfile

import scala.concurrent.ExecutionContext

object SlickModule {

  /** path in the **reference.conf** to obtain the path under which databases are configured.*/
  final val DbKeyConfig = "play.slick.db.config"

  /** path in the **reference.conf** to obtain the name of the default database.*/
  final val DefaultDbName = "play.slick.db.default"
}

@Singleton
final class SlickModule extends Module {
  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val config  = configuration.underlying
    val dbKey   = config.getString(SlickModule.DbKeyConfig)
    val default = config.getString(SlickModule.DefaultDbName)
    val dbs     = configuration.getOptional[Configuration](dbKey).getOrElse(Configuration.empty).subKeys
    Seq(bind[SlickApi].to[DefaultSlickApi].in[Singleton]) ++ namedDatabaseConfigBindings(dbs) ++ defaultDatabaseConfigBinding(
      default,
      dbs
    )
  }

  def namedDatabaseConfigBindings(dbs: Set[String]): Seq[Binding[_]] = dbs.toList.map { db =>
    bindNamed(db).to(new NamedDatabaseConfigProvider(db))
  }

  def defaultDatabaseConfigBinding(default: String, dbs: Set[String]): Seq[Binding[_]] =
    if (dbs.contains(default)) Seq(bind[DatabaseConfigProvider].to(bindNamed(default))) else Nil

  def bindNamed(name: String): BindingKey[DatabaseConfigProvider] =
    bind[DatabaseConfigProvider].qualifiedWith(new NamedDatabaseImpl(name))
}

/** Inject provider for named databases. */
final class NamedDatabaseConfigProvider(name: String) extends Provider[DatabaseConfigProvider] {
  @Inject private var slickApi: SlickApi = _

  lazy val get: DatabaseConfigProvider = new DatabaseConfigProvider {
    def get[P <: BasicProfile]: DatabaseConfig[P] = slickApi.dbConfig[P](DbName(name))
  }
}

trait SlickComponents {
  def environment: Environment
  def configuration: Configuration
  def applicationLifecycle: ApplicationLifecycle
  def executionContext: ExecutionContext

  lazy val slickApi: SlickApi = new DefaultSlickApi(environment, configuration, applicationLifecycle)(executionContext)
}
