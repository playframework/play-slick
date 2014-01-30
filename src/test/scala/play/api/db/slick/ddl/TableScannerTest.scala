//nested packages because test is reading packages

package play.api.db.slick.ddl {
  import org.specs2.mutable._

  class TableScannerTest extends Specification {
    val classloader = Thread.currentThread().getContextClassLoader()

    "TableScanner" should {
      "TODO: fill in text" in {
        TableScanner.reflectAllDDLMethods(Set("foo.models.*"), classloader)
        pending
      }
    }
  }
}

package foo.models {
  import scala.slick.driver.H2Driver.simple._
  
  class Foo(tag: Tag) extends Table[Long](tag, "FOO") {

    def id = column[Long]("ID")
    def * = id
  }
  
  object DAO {
    val Foo = TableQuery[Foo]
  }
}

