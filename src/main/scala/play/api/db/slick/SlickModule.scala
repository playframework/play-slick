package play.api.db.slick

import javax.inject.{ Inject, Singleton }
import play.api.{ Configuration, Environment, Mode }
import play.api.db.DBApi
import play.api.db.evolutions.DynamicEvolutions
import play.api.db.slick.ddl.SlickDDLException
import play.api.db.slick.ddl.TableScanner
import play.api.inject.Module
import play.api.libs.Files

@Singleton
class SlickModule extends Module {
  def bindings(environment: Environment, configuration: Configuration) = Seq(
    bind[DynamicEvolutions].to[SlickDynamicEvolutions],
    bind[SlickConfig].to[DefaultSlickConfig]
  )
}

trait SlickComponents {
  def environment: Environment
  def configuration: Configuration
  def dbApi: DBApi

  lazy val slickConfig: SlickConfig = new DefaultSlickConfig(configuration, environment, dbApi)
  lazy val dynamicEvolutions: DynamicEvolutions = new SlickDynamicEvolutions(slickConfig, environment)
}

@Singleton
class SlickDynamicEvolutions @Inject() (config: SlickConfig, environment: Environment) extends DynamicEvolutions {
  private val configKey = "slick"

  private val CreatedBy = "# --- Created by "

  def start(): Unit = {
    Config.set(config) // make global driver available for evolutions
    Database.cachedDatabases.clear() // clear resident databases
  }

  override def create(): Unit = {
    if (environment.mode != Mode.Prod) {
      for ((key, packageNames) <- config.packageNames) {
        val evolutions = environment.getFile("conf/evolutions/" + key + "/1.sql")
        if (!evolutions.exists() || Files.readFile(evolutions).startsWith(CreatedBy)) {
          try {
            evolutionScript(key, packageNames).foreach { evolutionScript =>
              Files.createDirectory(environment.getFile("conf/evolutions/" + key))
              Files.writeFileIfChanged(evolutions, evolutionScript)
            }
          } catch {
            case e: SlickDDLException => throw config.error(key, e)
          }
        }
      }
    }
  }

  def evolutionScript(driverName: String, names: Set[String]): Option[String] = {
    val driver = config.driver(driverName)
    val ddls = TableScanner.reflectAllDDLMethods(names, driver, environment.classLoader)

    val delimiter = ";" //TODO: figure this out by asking the db or have a configuration setting?

    if (ddls.nonEmpty) {
      val ddl = ddls
          .toSeq.sortBy(a => a.createStatements.mkString ++ a.dropStatements.mkString) //sort to avoid generating different schemas
          .reduceLeft((a, b) => a.asInstanceOf[driver.SchemaDescription] ++ b.asInstanceOf[driver.SchemaDescription])

      Some(CreatedBy + "Slick DDL\n" +
        "# To stop Slick DDL generation, remove this comment and start using Evolutions\n" +
        "\n" +
        "# --- !Ups\n\n" +
        ddl.createStatements.mkString("", s"$delimiter\n", s"$delimiter\n") +
        "\n" +
        "# --- !Downs\n\n" +
        ddl.dropStatements.mkString("", s"$delimiter\n", s"$delimiter\n") +
        "\n")
    } else None
  }

  start() // on construction
}
