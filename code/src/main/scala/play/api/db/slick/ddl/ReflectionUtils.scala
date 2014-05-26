package play.api.db.slick.ddl

import org.reflections.scanners
import org.reflections.util
import org.reflections.Reflections
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

object ReflectionUtils {
  import annotation.tailrec

  def getReflections(classloader: ClassLoader, pkg: String): Option[Reflections] = {
    val scanUrls = org.reflections.util.ClasspathHelper.forPackage(pkg, classloader)
    if (!scanUrls.isEmpty)
      Some(new Reflections(new util.ConfigurationBuilder()
        .addUrls(scanUrls)
        .filterInputsBy(new util.FilterBuilder().include(util.FilterBuilder.prefix(pkg + ".")))
        .setScanners(new scanners.TypeAnnotationsScanner, new scanners.TypesScanner)))
    else
      None
  }

  def splitIdentifiers(names: String) = names.split("""\.""").filter(!_.trim.isEmpty).toList
  def assembleIdentifiers(ids: Seq[String]) = ids.mkString(".")
  
  def findFirstModule(names: String)(implicit mirror: JavaMirror): Option[ModuleSymbol] = {

    // Check if a given ident is a module
    def symbolForIdent(ident: String): Option[ModuleSymbol] = {
      try {
        val symbol = mirror.staticModule(ident)
        mirror.reflectModule(symbol).instance //if we can reflect a module it means we are module 
        Some(symbol)
      } catch {
        case _: reflect.internal.MissingRequirementError => None
        //FIXME: must be another way to check if a static modules exists than exceptions!?!
        case _: ClassNotFoundException => None
        //We tried to reflect a module but got a class cast exception (again, would be nice to do this differently)
      }
    }

     // Produce a sequence of all the identifier prefixes of the given name
    val identifierSeqs: Seq[Seq[String]] = splitIdentifiers(names).scanLeft(Seq.empty[String]) {
      case (prefix, elem) => prefix :+ elem
    }.drop(1)
    
    // Get the first matching module, if any
    identifierSeqs.foldLeft(Option.empty[ModuleSymbol]) {
      case (found@Some(_), _) => found
      case (None, identifier) => symbolForIdent(assembleIdentifiers(identifier))
    }
  }
  

  def reflectModuleOrField(name: String, base: Any, baseSymbol: Symbol)(implicit mirror: JavaMirror): (Symbol, Any) = {
    val baseIM = mirror.reflect(base)
    val baseMember = baseSymbol.typeSignature.member(newTermName(name))
    val instance = if (baseMember.isModule) {
      if (baseMember.isStatic) {
        mirror.reflectModule(baseMember.asModule).instance
      } else {
        baseIM.reflectModule(baseMember.asModule).instance
      }
    } else {
      assert(baseMember.isTerm, "Expected " + baseMember + " to be something that can be reflected on " + base + " as a field")
      baseIM.reflectField(baseMember.asTerm).get
    }
    baseMember -> instance 
  }

  def scanModuleOrFieldByReflection( sym: Symbol, instance: Any)(checkSymbol: Symbol => Boolean)(implicit mirror: JavaMirror): List[(Symbol, Any)] = {
    @tailrec def scanModuleOrFieldByReflection(found: List[(Symbol, Any)],
      checked: Vector[Symbol],
      instancesNsyms: List[(Symbol, Any)]): List[(Symbol, Any)] = {

      val extractMembers: PartialFunction[(Symbol, Any), Iterable[(Symbol, Any)]] = {
        case (baseSym, baseInstance) =>
          if (baseInstance != null) {
            val vals = baseSym.typeSignature.members.filter(s => s.isModule || (s.isTerm && s.asTerm.isVal))
            vals.flatMap { mSym =>
              try {
                List(reflectModuleOrField(mSym.name.decoded, baseInstance, baseSym))
              } catch {
                case _: ScalaReflectionException => List.empty
              }
            }
          } else List.empty
      }
      val matching = instancesNsyms.flatMap(extractMembers).filter { case ( s, _) => checkSymbol(s) }
      val candidates = instancesNsyms.flatMap(extractMembers).filter { case (s, _) => !checkSymbol(s) && !checked.contains(s) }
      if (candidates.isEmpty)
        (found ++ matching).distinct
      else
        scanModuleOrFieldByReflection(found ++ matching, checked ++ (matching ++ candidates).map(_._1), candidates)
    }

    scanModuleOrFieldByReflection(List.empty, Vector.empty, List(sym -> instance))
  }

}
