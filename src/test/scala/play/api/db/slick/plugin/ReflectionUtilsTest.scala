package play.api.db.slick.plugin

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test._
import play.api.Play.current
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

package models {
  object A {
    object B {
      object C
    }
  }

  package D {
    object E
  }

  package G {
    package H {
      package I {}
    }
  }
}

class ReflectionUtilsTest extends Specification {

  "ReflectionUtils.splitIdentifier" should {
    "return splitted identifier" in {
      ReflectionUtils.splitIdentifiers("play.api.db") must_== List("play", "api", "db")
    }
  }

  "ReflectionUtils.findFirstModule" should {
    "convert class name to Option[ModuleSymbol]" in {
      running(FakeApplication()) {
        implicit val mirror = universe.runtimeMirror(current.classloader)
        ReflectionUtils.findFirstModule("play.api.db.slick.plugin.models.A").map(_.toString) must beSome("object A")
      }
    }

  }

  "ReflectionUtils.findFirstModule" should {
    "return first module" in {
      running(FakeApplication()) {
        implicit val mirror = universe.runtimeMirror(current.classloader)

        ReflectionUtils.findFirstModule("play.api.db.slick.plugin.models.A.B.C").map(_.toString) must beSome("object A")
        ReflectionUtils.findFirstModule("play.api.db.slick.plugin.models.D.E.F").map(_.toString) must beSome("object E")
        ReflectionUtils.findFirstModule("play.api.db.slick.plugin.models.G.H.I").map(_.toString) must beNone
      }
    }
  }

}
