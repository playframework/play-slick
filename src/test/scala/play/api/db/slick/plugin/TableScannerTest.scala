//uses nested package structure

import org.specs2.mutable._
import play.api.db.slick.plugin.TableScanner

package play.api.db.slick.objectreflection {
  object TestTables {
    val bar = new play.api.db.slick.packagereflection.classes.Bar
  }
}

package play.api.db.slick.packagereflection.objects {
  import slick.driver.H2Driver.simple._

  object Foo extends Table[(String)]("FOO") {
    def a = column[String]("A")
    def * = a
  }

}

package play.api.db.slick.packagereflection.classes {
  import slick.driver.H2Driver.simple._

  class Bar extends Table[(String)]("BAR") {
    def a = column[String]("A")
    def * = a
  }

}

package play.api.db.slick.plugin {

  class TableScannerTest extends Specification {
    "Scanning for tables" should {
      "detect objects in packages using wildcard" in {
        val classloader = Thread.currentThread().getContextClassLoader()
        val results = TableScanner.reflectAllDDLMethods(Set("play.api.db.slick.packagereflection.objects.*"), classloader).map(_.createStatements.mkString(";"))
        results.toList === List("""create table "FOO" ("A" VARCHAR NOT NULL)""")
      }

      "detect classes in objects using wildcard" in {
        val classloader = Thread.currentThread().getContextClassLoader()
        val results = TableScanner.reflectAllDDLMethods(Set("play.api.db.slick.objectreflection.TestTables.*"), classloader).map(_.createStatements.mkString(";"))
        results.toList === List("""create table "BAR" ("A" VARCHAR NOT NULL)""")
      }

      "detect classes using wildcard" in {
        val classloader = Thread.currentThread().getContextClassLoader()
        val results = TableScanner.reflectAllDDLMethods(Set("play.api.db.slick.packagereflection.classes.*"), classloader).map(_.createStatements.mkString(";"))
        results.toList === List("""create table "BAR" ("A" VARCHAR NOT NULL)""")
      }
    }
  }
}