//nested packages because test is reading packages

import scala.slick.driver.JdbcProfile

package play.api.db.slick.ddl {
  package object test {
    val driver = scala.slick.driver.H2Driver
  }
  import org.specs2.mutable._

  class TableScannerTest extends Specification {
    val classloader = Thread.currentThread().getContextClassLoader()

    "TableScanner" should {
      "scan classes in packages" in {
        TableScanner.reflectAllDDLMethods(Set("no.dao.Foo"), play.api.db.slick.ddl.test.driver, classloader) must have size (1)
        TableScanner.reflectAllDDLMethods(Set("no.dao.*"), play.api.db.slick.ddl.test.driver, classloader) must have size (1)
      }
      
      "find instances of table queries in DAO objects" in {
        TableScanner.reflectAllDDLMethods(Set("basic.dao.*"), play.api.db.slick.ddl.test.driver, classloader) must have size (1)
        TableScanner.reflectAllDDLMethods(Set("basic.dao.DAO.Foo"), play.api.db.slick.ddl.test.driver, classloader) must have size (1)
      }
      
      "find instances of table queries when cake pattern is used" in {
        TableScanner.reflectAllDDLMethods(Set("cake.profile.current.*"), play.api.db.slick.ddl.test.driver, classloader) must have size (1)
        TableScanner.reflectAllDDLMethods(Set("cake.profile.current.dao.Foo"), play.api.db.slick.ddl.test.driver, classloader) must have size (1)
      }
      
      "find instances of table queries when (semi-)cake pattern is usesd (like in computer database)" in {
        TableScanner.reflectAllDDLMethods(Set("cake.computer.database.*"), play.api.db.slick.ddl.test.driver, classloader) must have size (2)
      }
      
      "not fail if there is nothing is found (errors will be printed)" in {
        TableScanner.reflectAllDDLMethods(Set("blah.blah.Zoo"), play.api.db.slick.ddl.test.driver, classloader) must have size (0)
        //prints out logging error (which is expected)
        TableScanner.reflectAllDDLMethods(Set("blah.blah.*"), play.api.db.slick.ddl.test.driver, classloader) must have size (0)
      }
    }
  }
}

package no.dao {
  import play.api.db.slick.ddl.test.driver.simple._

  class Foo(tag: Tag) extends Table[Long](tag, "FOO") {

    def id = column[Long]("ID")
    def * = id
  }

}

package basic.dao {
  import play.api.db.slick.ddl.test.driver.simple._

  class Foo(tag: Tag) extends Table[Long](tag, "FOO") {

    def id = column[Long]("ID")
    def * = id
  }

  object DAO {
    val Foo = TableQuery[Foo]
  }
}

package cake.profile {
  import play.api.db.slick.Profile
  package all {
    trait FooComponent { this: Profile =>
      import profile.simple._

      class Foo(tag: Tag) extends Table[Long](tag, "FOO") {
        def id = column[Long]("ID")
        def * = id
      }
    }
  }

  class DAO(override val profile: JdbcProfile) extends all.FooComponent with Profile {
    import profile.simple._
    val Foo = TableQuery[Foo]
  }

  object current {
    val dao = new DAO(play.api.db.slick.ddl.test.driver)
  }

  object another {
    val dao = new DAO(play.api.db.slick.ddl.test.driver)
  }

}

package cake.computer.database {
  import play.api.db.slick.ddl.test.driver.simple._

  private[database] trait DAO {
    val Companies = TableQuery[Companies]
    val Computers = TableQuery[Computers]
  }
  class Companies(tag: Tag) extends Table[Long](tag, "COMPANIES") {
    def id = column[Long]("ID")
    def * = id
  }

  class Computers(tag: Tag) extends Table[Long](tag, "COMPUTER") {
    def id = column[Long]("ID")
    def * = id
  }

  object Companies extends DAO {

  }

  object Computers extends DAO {

  }

}

