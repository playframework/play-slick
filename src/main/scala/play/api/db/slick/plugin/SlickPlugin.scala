package play.api.db.slick.plugin

import play.api.Application
import play.api.Plugin
import play.api.libs.Files
import play.api.Mode
import scala.slick.lifted.DDL
import scala.Option.option2Iterable
import scala.annotation.tailrec
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.JavaMirror
import scala.reflect.runtime.universe.ModuleSymbol
import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.universe.newTermName

class SlickDDLException(val message: String) extends Exception(message)

class SlickDDLPlugin(app: Application) extends Plugin {

  private val configKey = "slick"

  private def isDisabled: Boolean = app.configuration.getString("evolutionplugin").map(_ == "disabled").headOption.getOrElse(false)

  override def enabled = !isDisabled

  override def onStart(): Unit = {
    val conf = app.configuration.getConfig(configKey)
    conf.foreach { conf =>
      conf.keys.foreach { key =>
        val packageNames = conf.getString(key).getOrElse(throw conf.reportError(key, "Expected key " + key + " but could not get its values!", None)).split(",").toSet
        if (app.mode != Mode.Prod) {
          val evolutionsEnabled = !"disabled".equals(app.configuration.getString("evolutionplugin"))
          if (evolutionsEnabled) {
            val evolutions = app.getFile("conf/evolutions/" + key + "/1.sql");
            if (!evolutions.exists() || Files.readFile(evolutions).startsWith(CreatedBy)) {
              try {
                evolutionScript(packageNames).foreach { evolutionScript =>
                  Files.createDirectory(app.getFile("conf/evolutions/" + key));
                  Files.writeFileIfChanged(evolutions, evolutionScript);
                }
              } catch {
                case e: SlickDDLException => throw conf.reportError(key, e.message, Some(e))
              }
            }
          }
        }
      }
    }
  }

  private val CreatedBy = "# --- Created by "

  def evolutionScript(names: Set[String]): Option[String] = {
    val classloader = app.classloader

    import scala.collection.JavaConverters._
    val ddls = TableScanner.reflectAllDDLMethods(names, classloader)

    val delimiter = ";" //TODO: figure this out by asking the db or have a configuration setting?

    if (ddls.nonEmpty) {
      val ddl = ddls.reduceLeft(_ ++ _)

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

}
