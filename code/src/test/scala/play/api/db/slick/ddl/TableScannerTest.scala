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

      "find instances of table queries in the pets example" in {
        val ddls = TableScanner.reflectAllDDLMethods(Set("cake.pets.current.*"), play.api.db.slick.ddl.test.driver, classloader)
        ddls must have size (2)

        val flatDDLs = ddls.map(_.createStatements.mkString).mkString
        flatDDLs must contain("CAT")
        flatDDLs must contain("DOG")
      }

      "find instances of table queries in the store example" in {
        val ddls = TableScanner.reflectAllDDLMethods(Set("cake.store.current.*"), play.api.db.slick.ddl.test.driver, classloader)
        ddls must have size (2)

        val flatDDLs = ddls.map(_.createStatements.mkString).mkString
        flatDDLs must contain("ORDER")
        flatDDLs must contain("CUSTOMER")
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

  object Companies extends DAO
  object Computers extends DAO
}

package cake.pets {
  import play.api.db.slick.Profile

  case class Cat(name: String, color: String)
  case class Dog(name: String, color: String)

  trait CatComponent { this: Profile =>
    import profile.simple._

    class CatsTable(tag: Tag) extends Table[Cat](tag, "CAT") {

      def name = column[String]("name", O.PrimaryKey)
      def color = column[String]("color", O.NotNull)

      def * = (name, color) <> (Cat.tupled, Cat.unapply _)
    }
  }

  trait DogComponent { this: Profile =>
    import profile.simple._

    class DogsTable(tag: Tag) extends Table[Dog](tag, "DOG") {

      def name = column[String]("name", O.PrimaryKey)
      def color = column[String]("color", O.NotNull)

      def * = (name, color) <> (Dog.tupled, Dog.unapply _)
    }
  }

  class DAO(override val profile: JdbcProfile) extends Profile
      with CatComponent
      with DogComponent {

    import profile.simple._
    val Cats = TableQuery[CatsTable]
    val Dogs = TableQuery[DogsTable]
  }

  object current {
    val dao = new DAO(play.api.db.slick.ddl.test.driver)
  }
}

package cake.store {
  import play.api.db.slick.Profile

  case class Customer(name: String)
  case class Order(customerName: String, product: String)

  trait CustomerComponent { self: Profile =>
    import profile.simple._

    class CustomerTable(tag: Tag) extends Table[Customer](tag, "CUSTOMER") {
      def name = column[String]("name", O.PrimaryKey)
      def * = (name) <> (Customer.apply, Customer.unapply _)
    }

    val Customers = TableQuery[CustomerTable]
  }

  trait OrderComponent { self: Profile with CustomerComponent =>
    import profile.simple._

    class OrderTable(tag: Tag) extends Table[Order](tag, "ORDER") {
      def customerName = column[String]("customerName", O.PrimaryKey)
      def customer = foreignKey("FK_ORDER_CUSTOMER", customerName, Customers)(_.name)
      def product = column[String]("product", O.NotNull)

      def * = (customerName, product) <> (Order.tupled, Order.unapply _)
    }

    // Should be working with object declaration also
    object Orders extends TableQuery(new OrderTable(_))
  }

  class DAO(override val profile: JdbcProfile) extends Profile
    with CustomerComponent
    with OrderComponent

  object current {
    val dao = new DAO(play.api.db.slick.ddl.test.driver)
  }
}

