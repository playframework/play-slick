package play.api.db.slick

import play.api.Application
import play.api.Plugin
import play.libs.ReflectionsCache
import org.reflections.scanners.TypesScanner
import scala.slick.session.Database
import play.api.libs.Files
import play.api.Mode

class SlickDDLPlugin(app: Application) extends Plugin {
  private val configKey = "slick"
  
  private def isDisabled = app.configuration.getBoolean("evolutionplugin").getOrElse(false)
  
  override def enabled = !isDisabled

  override def onStart(): Unit = {
    val conf = app.configuration.getConfig(configKey)
    conf.foreach { conf =>
      conf.keys.foreach { key =>
        val packageNames = conf.getString(key).getOrElse(throw conf.reportError(key, "Expected key " + key + " but could not get its values!", None)).split(",").toSet
        if(app.mode != Mode.Prod) {
          val evolutionsEnabled = !"disabled".equals(app.configuration.getString("evolutionplugin"))
          if(evolutionsEnabled) {
            val evolutions = app.getFile("conf/evolutions/" + key + "/1.sql");
            if(!evolutions.exists() || Files.readFile(evolutions).startsWith(CreatedBy)) {
              evolutionScript(packageNames).foreach{ evolutionScript =>
                Files.createDirectory(app.getFile("conf/evolutions/" + key));
                Files.writeFileIfChanged(evolutions, evolutionScript);
              }
            }
          }
        }
      }
    }
  }
  
  private val CreatedBy = "# --- Created by "

  private val WildcardPattern = """(.*)\.\*""".r
    
  def evolutionScript(packageNames: Set[String]): Option[String] = {
    val classloader = app.classloader
    
    import scala.collection.JavaConverters._
    
    val classNames = packageNames.flatMap { packageName =>
      packageName match {
        case WildcardPattern(p) => ReflectionsCache.getReflections(classloader, p) //TODO: would be nicer if we did this using Scala reflection
          .getStore
          .get(classOf[TypesScanner])
          .keySet.asScala.toSet
        case p => Set(p)
      }
    }
    
    val ddls = reflectAllDDLMethods(classNames, classloader)
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

  def reflectAllDDLMethods(classNames: Set[String], classloader: ClassLoader) = {
    import scala.reflect.runtime.universe
    import scala.reflect.runtime.universe._
    
    val tableType = typeOf[slick.driver.BasicTableComponent#Table[_]]
    val ddlMethodName = "ddl"
    
    val mirror = universe.runtimeMirror(classloader)

    classNames.flatMap { className =>
    
      val moduleSymbol = try{ //TODO: ideally we should be able to test for existence not use exceptions
        Some(mirror.staticModule(className))
      } catch {
        case e: scala.reflect.internal.MissingRequirementError => None
      }
      
      moduleSymbol.flatMap { moduleSymbol =>
        val isSubclassedByTableType = moduleSymbol.typeSignature.baseClasses.find(_.typeSignature == tableType.typeSymbol.typeSignature).isDefined
        
        if (isSubclassedByTableType) {
          val im = mirror.reflect(mirror.reflectModule(moduleSymbol.asModule).instance)
          val ddlMethodSymbol = moduleSymbol.typeSignature.member(newTermName(ddlMethodName)).asMethod
          if (ddlMethodSymbol.isMethod) {
            val ddlReflected = im.reflectMethod(ddlMethodSymbol)
            val ddl = ddlReflected().asInstanceOf[scala.slick.lifted.DDL]
            Some(ddl) //found a correctly named method on an object that extends Table, assuming we are good...
          } else None
        } else None
      }
    }
  }
}