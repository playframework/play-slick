package play.api.db.slick.test

import play.api.db.slick._
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

  "findFirstModule" should {
    "return first module" in {
      running(FakeApplication()) {
        implicit val mirror = universe.runtimeMirror(current.classloader)

        ReflectionUtils.findFirstModule("play.api.db.slick.test.models.A.B.C").map(_.toString) must beSome("object A")
        ReflectionUtils.findFirstModule("play.api.db.slick.test.models.D.E.F").map(_.toString) must beSome("object E")
        ReflectionUtils.findFirstModule("play.api.db.slick.test.models.G.H.I").map(_.toString) must beNone
      }
    }
  }

}
