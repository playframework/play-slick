package play.api.db.slick

import scala.collection.immutable.Seq

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import play.api.Configuration
import play.api.Environment
import play.api.Mode
import play.api.PlayException
import play.api.db.DBApi
import play.api.db.evolutions.DynamicEvolutions
import play.api.db.slick.ddl.SlickDDLException
import play.api.db.slick.ddl.TableScanner
import play.api.db.slick.internal.DBApiAdapter
import play.api.inject.ApplicationLifecycle
import play.api.inject.Binding
import play.api.inject.BindingKey
import play.api.inject.Module
import play.api.libs.Files
import play.db.NamedDatabaseImpl
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.profile.BasicProfile

object SlickModule {
  final val DbKeyConfig = "play.slick.db.config"
  final val DefaultDbName = "play.slick.db.default"
}

@Singleton
final class SlickModule extends Module {
  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val config = configuration.underlying
    val dbKey = config.getString(SlickModule.DbKeyConfig)
    val default = config.getString(SlickModule.DefaultDbName)
    val dbs = configuration.getConfig(dbKey).getOrElse(Configuration.empty).subKeys
    Seq(
     bind[SlickApi].to[DefaultSlickApi].in[Singleton],
     bind[DBApi].to[DBApiAdapter].in[Singleton],
     bind[DynamicEvolutions].to[SlickDynamicEvolutions].in[Singleton]
     ) ++ namedDatabaseConfigBindings(dbs) ++ defaultDatabaseConfigBinding(default, dbs)
  }

  def namedDatabaseConfigBindings(dbs: Set[String]): Seq[Binding[_]] = dbs.toList.map { db =>
    bindNamed(db).to(new NamedDatabaseConfigProvider(db))
  }

  def defaultDatabaseConfigBinding(default: String, dbs: Set[String]): Seq[Binding[_]] =
    if (dbs.contains(default)) Seq(bind[DatabaseConfigProvider].to(bindNamed(default))) else Nil

  def bindNamed(name: String): BindingKey[DatabaseConfigProvider] =
    bind(classOf[DatabaseConfigProvider]).qualifiedWith(new NamedDatabaseImpl(name))
}

/**
 * Inject provider for named databases.
 */
final class NamedDatabaseConfigProvider(name: String) extends Provider[DatabaseConfigProvider] {
  @Inject private var slickApi: SlickApi = _

  lazy val get: DatabaseConfigProvider = new DatabaseConfigProvider {
    def get[P <: BasicProfile]: DatabaseConfig[P] = slickApi.dbConfig[P](name)
  }
}

trait SlickComponents {
  def environment: Environment
  def configuration: Configuration
  def applicationLifecycle: ApplicationLifecycle

  lazy val api: SlickApi = new DefaultSlickApi(environment, configuration, applicationLifecycle)
  lazy val dynamicEvolutions: DynamicEvolutions = new SlickDynamicEvolutions(configuration, api, environment)
}

final class SlickDynamicEvolutions @Inject() (config: Configuration, slickApi: SlickApi, environment: Environment) extends DynamicEvolutions {
  private val CreatedBy = "# --- Created by "

  private val logger = play.api.Logger(classOf[SlickDynamicEvolutions])

  private val slickEvolutionsKey = config.underlying.getString("play.slick.evolutions.config")

  @throws(classOf[PlayException])
  private lazy val packageNames: Map[String, Set[String]] = {
    def keyNotFound(key: String): Nothing =
      throw config.reportError(key, "Expected key " + key + " but could not get its values!")

    (for {
      conf <- config.getConfig(slickEvolutionsKey).toSeq
      key <- conf.keys
      names = conf.getString(key).getOrElse(keyNotFound(key)).split("""(,\n|\n|,)""").toSet
    } yield {
      key -> names
    }).toMap
  }

  override def create(): Unit = {
    logger.debug("calling create on evolutions")
    if (environment.mode != Mode.Prod) {
      logger.debug("packaged : " + packageNames)
      for ((key, packageNames) <- packageNames) {
        val evolutions = environment.getFile("conf/evolutions/" + key + "/1.sql")
        if (!evolutions.exists() || Files.readFile(evolutions).startsWith(CreatedBy)) {
          try {
            evolutionScript(key, packageNames).foreach { evolutionScript =>
              Files.createDirectory(environment.getFile("conf/evolutions/" + key))
              Files.writeFileIfChanged(evolutions, evolutionScript)
            }
          } catch {
            case e: SlickDDLException => throw config.globalError(key, Some(e))
          }
        }
      }
    }
  }

  protected def evolutionScript(dbName: String, names: Set[String]): Option[String] = {
    val driver = Option(slickApi.dbConfig(dbName).driver).getOrElse(throw config.reportError(s"$slickEvolutionsKey.$dbName.driver", "No Slick driver defined.")).asInstanceOf[JdbcProfile]
    val schemas = TableScanner.reflectAllDDLMethods(names, driver, environment.classLoader)

    val delimiter = ";" //TODO: figure this out by asking the db or have a configuration setting?

    if (schemas.nonEmpty) {
      val schema = schemas
        .toSeq.sortBy(a => a.createStatements.mkString ++ a.dropStatements.mkString) //sort to avoid generating different schemas
        .reduceLeft((a, b) => a.asInstanceOf[driver.SchemaDescription] ++ b.asInstanceOf[driver.SchemaDescription])

      Some(CreatedBy + "Slick DDL\n" +
        "# To stop Slick DDL generation, remove this comment and start using Evolutions\n" +
        "\n" +
        "# --- !Ups\n\n" +
        schema.createStatements.mkString("", s"$delimiter\n", s"$delimiter\n") +
        "\n" +
        "# --- !Downs\n\n" +
        schema.dropStatements.mkString("", s"$delimiter\n", s"$delimiter\n") +
        "\n")
    } else None
  }
}