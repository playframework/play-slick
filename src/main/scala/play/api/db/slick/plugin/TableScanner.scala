package play.api.db.slick.plugin

import scala.slick.lifted.DDL
import play.libs.ReflectionsCache
import org.reflections.scanners.TypesScanner

object TableScanner {
  import scala.reflect.runtime.universe
  import scala.reflect.runtime.universe._
  
  lazy val tableType = typeOf[slick.driver.BasicTableComponent#Table[_]]

  private def isTable(sym: Symbol) = {
    sym.typeSignature.baseClasses.find(_.typeSignature == tableType.typeSymbol.typeSignature).isDefined
  }

  private def tableToDDL(instance: Any): (java.lang.Class[_], DDL) = {
    import scala.language.reflectiveCalls //this is the simplest way to do achieve this, we are using reflection either way...
    instance.getClass -> instance.asInstanceOf[{ def ddl: DDL }].ddl
  }

  private def scanModulesAndFields(baseSym: ModuleSymbol, name: String)(implicit mirror: Mirror) = {
    val baseInstance = mirror.reflectModule(baseSym).instance

    val allIds = ReflectionUtils.splitIdentifiers(name.replace(baseSym.fullName, ""))
    val isWildCard = allIds.lastOption.map(_ == "*").getOrElse(false)
    val ids = if (isWildCard) allIds.init else allIds

    val (outerInstance, outerSym) = ids.foldLeft(baseInstance -> (baseSym: Symbol)) {
      case ((instance, sym), id) =>
        ReflectionUtils.reflectModuleOrField(id, instance, sym)
    }

    val foundInstances = if (isTable(outerSym) && !isWildCard) {
      List(outerInstance)
    } else if (isWildCard) {
      val instancesNsyms = ReflectionUtils.scanModuleOrFieldByReflection(outerInstance, outerSym)(isTable)
      if (instancesNsyms.isEmpty) play.api.Logger.warn("Scanned object: '" + baseSym.fullName + "' for '" + name + "' but did not find any Slick Tables")
      instancesNsyms.map(_._1)
    } else {
      throw new SlickDDLException("Found a matching object: '" + baseSym.fullName + "' for '" + name + "' but it is not a Slick Table and a wildcard was not specified")
    }
    foundInstances.map { instance => tableToDDL(instance) }
  }

  private val WildcardPattern = """(.*)\.\*""".r
  
  private def reflectModule(className: String)(implicit mirror: Mirror) = {
    val moduleSymbol = mirror.staticModule(className)
    if (isTable(moduleSymbol)) {
      Some(tableToDDL(mirror.reflectModule(moduleSymbol).instance))
    } else {
      None
    }
  }

  private def reflectClass(className: String)(implicit mirror: Mirror) = {
    val classSymbol = mirror.staticClass(className)
    val constructorSymbol = classSymbol.typeSignature.declaration(universe.nme.CONSTRUCTOR)
    if (isTable(classSymbol) && constructorSymbol.isMethod) {
      val constructorMethod = constructorSymbol.asMethod
      val reflectedClass = mirror.reflectClass(classSymbol)
      val constructor = reflectedClass.reflectConstructor(constructorMethod)
      try {
        val instance = constructor()
        Some(tableToDDL(instance))
      } catch {
        case e: java.lang.IllegalArgumentException =>
          play.api.Logger.warn("Found a Slick table: " + className + ", but it has not a constructor with no arguments. Cannot create DDL for this class")
          None
      }
    } else {
      None
    }
  }
  
  private def scanPackages(name: String)(implicit mirror: Mirror) = {
    import scala.collection.JavaConverters._
    
    val classloader = mirror.classLoader
    val classNames = name match {
      case WildcardPattern(p) => ReflectionsCache.getReflections(classloader, p) //TODO: would be nicer if we did this using Scala reflection, alas staticPackage is non-deterministic:  https://issues.scala-lang.org/browse/SI-6573
        .getStore
        .get(classOf[TypesScanner])
        .keySet.asScala.toSet
      case p => Set(p)
    }
    classNames.flatMap { className =>
    	  try { //FIXME: ideally we should be able to test for existence not use exceptions
        reflectModule(className) orElse reflectClass(className)
      } catch {
        case e: scala.reflect.internal.MissingRequirementError => None
      }
    }
  }
  
  /**
   * Reflect all DDL methods found for a set of names with wildcards used to scan for Slick Table classes, objects and packages 
   */
  def reflectAllDDLMethods(names: Set[String], classloader: ClassLoader): Seq[DDL] = synchronized { //reflection API not thread-safe
    implicit val mirror = universe.runtimeMirror(classloader)
    
    val classesAndNames = names.flatMap { name =>
      ReflectionUtils.findFirstModule(name) match {
        case Some(baseSym) => scanModulesAndFields(baseSym, name)
        case _ => scanPackages(name)
      }
    }

    classesAndNames.toSeq.sortBy(_._1.toString).map(_._2).distinct
  }
}