package play.api.db.slick.ddl

import scala.slick.driver.JdbcDriver
import scala.slick.lifted.AbstractTable
import org.apache.xerces.dom3.as.ASModel
import scala.slick.driver.JdbcProfile
import scala.slick.lifted.Tag

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._
import scala.slick.SlickException

class SlickDDLException(val message: String) extends Exception(message)

object TableScanner {
  lazy val logger = play.api.Logger(TableScanner.getClass)

  type DDL = scala.slick.profile.SqlProfile#DDL

  private def subTypeOf(sym: Symbol, subTypeSymbol: Symbol) = {
    sym.typeSignature.baseClasses.find(_ == subTypeSymbol).isDefined
  }

  private def scanModulesAndFields(driver: JdbcDriver, baseSym: ModuleSymbol, name: String)(implicit mirror: Mirror): Set[(Symbol, DDL)] = {
    logger.debug("scanModulesAndFields for: " + name)

    val baseInstance = mirror.reflectModule(baseSym).instance

    val allIds = ReflectionUtils.splitIdentifiers(name.replace(baseSym.fullName, ""))
    val isWildCard = allIds.lastOption.map(_ == "*").getOrElse(false)
    val ids = if (isWildCard) allIds.init else allIds

    val (outerSym, outerInstance) = ids.foldLeft((baseSym: Symbol)-> baseInstance) {
      case ((sym, instance), id) =>
        ReflectionUtils.reflectModuleOrField(id, instance, sym)
    }

    val tableQueryTypeSymbol = {
      import driver.simple._
      typeOf[TableQuery[_]].typeSymbol
    }

    val foundInstances: List[(Symbol, Any)] = if (subTypeOf(outerSym, tableQueryTypeSymbol) && !isWildCard) { //name was referencing a specific value
      logger.debug("scanModulesAndFields for: found table query instance (not wildcard): " + name)
      List(baseSym -> outerInstance)
    } else if (isWildCard) { //wildcard so we scan the instance we found for table queries
      val instancesNsyms = ReflectionUtils.scanModuleOrFieldByReflection(outerSym, outerInstance)(subTypeOf(_, tableQueryTypeSymbol))
      if (instancesNsyms.isEmpty) logger.warn("Scanned object: '" + baseSym.fullName + "' for '" + name + "' but did not find any Slick Tables")
      logger.debug("scanModulesAndFields for: found " + instancesNsyms.size + " sub-instances (wildcard): " + name)
      instancesNsyms
    } else {
      throw new SlickDDLException("Found a matching object: '" + baseSym.fullName + "' for '" + name + "' but it is not a Slick Table and a wildcard was not specified")
    }

    foundInstances.map { case (name, instance) =>
      import driver.simple._
      name -> logSlickException(instance.asInstanceOf[TableQuery[Table[_]]].ddl)
    }.toSet
  }

  private def logSlickException[A](func: => A): A = {
    try {
      func
    } catch {
      case e: SlickException =>
        logger.error("Got an error converting to DDL. Check whether the profile used for the Table/TableQuery is the same as the one used by DDL generation.")
        throw e
    }
  }

  private def classToDDL(driver: JdbcDriver, className: String, tableSymbol: Symbol)(implicit mirror: Mirror): Option[(Symbol, DDL)] = {
    try {
      logger.debug("classToDDL for: " + className)
      val classSymbol = mirror.staticClass(className)
      val constructorSymbol = classSymbol.typeSignature.declaration(universe.nme.CONSTRUCTOR)
      if (subTypeOf(classSymbol, tableSymbol) && constructorSymbol.isMethod) {
        logger.debug("classToDDL for: " + className + " is table and has constuctor")
        val constructorMethod = constructorSymbol.asMethod
        val reflectedClass = mirror.reflectClass(classSymbol)
        val constructor = reflectedClass.reflectConstructor(constructorMethod)
        import driver.simple._
        logSlickException {
          Some(classSymbol -> TableQuery { tag =>
            val instance = constructor(tag)
            instance.asInstanceOf[Table[_]]
          }.ddl)
        }
      } else {
        None
      }
    } catch {
      case e: java.lang.IllegalArgumentException =>
        logger.warn("Found a Slick table: " + className + ", but it does not have a constructor without arguments. Cannot create DDL for this class")
        None
      case e: java.lang.InstantiationException =>
        logger.warn("Could not initialize " + className + ". DDL Generation will be skipped.")
        None
      case e: scala.reflect.internal.MissingRequirementError =>
        logger.debug("MissingRequirementError for " + className + ". Probably means this is not a class. DDL Generation will be skipped.")
        None
    }
  }

  val WildcardPattern = """(.*)\.\*""".r

  /**
   * Returns the names of objects/classes in a package
   */
  def scanPackage(name: String, classloader: ClassLoader): Set[String] = {
    import scala.collection.JavaConverters._
    ReflectionUtils.getReflections(classloader, name).map { reflections =>
      reflections.getStore //TODO: would be nicer if we did this using Scala reflection, alas staticPackage is non-deterministic:  https://issues.scala-lang.org/browse/SI-6573
        .get(classOf[org.reflections.scanners.TypesScanner])
        .keySet.asScala.toSet[String]
    }.toSet.flatten
  }

  /**
   * Reflect all DDL methods found for a set of names with wildcards used to scan for Slick Table classes, objects and packages
   */
  def reflectAllDDLMethods(names: Set[String], driver: JdbcDriver, classloader: ClassLoader): Set[DDL] = synchronized { //reflection API not thread-safe
    implicit val mirror = universe.runtimeMirror(classloader)

    val tableTypeSymbol = typeOf[AbstractTable[_]].typeSymbol

    val ddls = names.flatMap { name =>
      val maybeModule: Option[ModuleSymbol] = ReflectionUtils.findFirstModule(name)
      val currentDDLs = maybeModule match {
        case Some(moduleSymbol) =>
          logger.debug(name + " is a module: scanning... ")
          val instaniatedDDLs = scanModulesAndFields(driver, moduleSymbol, name)
          instaniatedDDLs
        case None =>
          logger.debug(name + " is not a module: checking if wildcard and converting classes...")
          val classDDLs = (name match {
            case WildcardPattern(packageName) => scanPackage(packageName, classloader)
            case name => Set(name)
          }).flatMap {
            classToDDL(driver, _, tableTypeSymbol)
          }
          classDDLs
      }
      if (currentDDLs.isEmpty)
        logger.error("Could not find any classes or table queries for: " + name + "")
      currentDDLs
    }

    ddls.groupBy(_._1.fullName).flatMap {
      case (name, ddls) =>
        if (ddls.size > 1) logger.warn(s"Found multiple ddls ${ddls.size} for: $name")
        ddls.headOption.map(_._2)
    }.toSet
  }

}