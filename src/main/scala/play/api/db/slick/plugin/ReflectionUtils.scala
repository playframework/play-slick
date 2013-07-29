package play.api.db.slick.plugin


object ReflectionUtils {
  import annotation.tailrec
  import scala.reflect.runtime.universe
  import scala.reflect.runtime.universe._

  def splitIdentifiers(names: String) = names.split("""\.""").filter(!_.trim.isEmpty).toList
  def assembleIdentifiers(ids: List[String]) = ids.mkString(".")

  def findFirstModule(names: String)(implicit mirror: JavaMirror): Option[ModuleSymbol] = {
    val elems = splitIdentifiers(names)
    var i = 1 //FIXME: vars...
    var res: Option[ModuleSymbol] = None
    while (i < (elems.size + 1) && !res.isDefined) {
      try {
        res = Some(mirror.staticModule(assembleIdentifiers(elems.slice(0, i))))
      } catch {
        case e: reflect.internal.MissingRequirementError =>
        //FIXME: must be another way to check if a static modules exists than exceptions!?!
      } finally {
        i += 1
      }
    }
    res
  }

  def reflectModuleOrField(name: String, base: Any, baseSymbol: Symbol)(implicit mirror: JavaMirror) = {
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
    instance -> baseMember
  }

  def scanModuleOrFieldByReflection(instance: Any, sym: Symbol)(checkSymbol: Symbol => Boolean)(implicit mirror: JavaMirror): List[(Any, Symbol)] = {
    @tailrec def scanModuleOrFieldByReflection(found: List[(Any, Symbol)],
      checked: Vector[Symbol],
      instancesNsyms: List[(Any, Symbol)]): List[(Any, Symbol)] = {

      val extractMembers: PartialFunction[(Any, Symbol), Iterable[(Any, Symbol)]] = {
        case (baseInstance, baseSym) =>
          if (baseInstance != null) {
            baseSym.typeSignature.members.filter(s => s.isModule || (s.isTerm && s.asTerm.isVal)).map { mSym =>
              reflectModuleOrField(mSym.name.decoded, baseInstance, baseSym)
            }
          } else List.empty
      }
      val matching = instancesNsyms.flatMap(extractMembers).filter { case (_, s) => checkSymbol(s) }
      val candidates = instancesNsyms.flatMap(extractMembers).filter { case (_, s) => !checkSymbol(s) && !checked.contains(s) }
      if (candidates.isEmpty)
        (found ++ matching).distinct
      else
        scanModuleOrFieldByReflection(found ++ matching, checked ++ (matching ++ candidates).map(_._2), candidates)
    }

    scanModuleOrFieldByReflection(List.empty, Vector.empty, List(instance -> sym))
  }

}