package play.api.db.slick

import scala.concurrent.ExecutionContext

import com.jolbox.bonecp.BoneCPDataSource
import java.sql.DriverManager
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc.Results._
import scala.slick.driver.H2Driver

class TestableDBActionSpec extends Specification {

  // Force H2 driver registration, to fix "java.sql.SQLException: No suitable driver found".
  // Probably caused by overeager driver deregistration in a previous test. It may be
  // possible to remove this line and the line in SlickPlayIterateesFunctionalTest in the near future
  // once a DB plugin fix makes it into Play: https://github.com/playframework/playframework/pull/2794
  DriverManager.registerDriver(new org.h2.Driver())

  val datasource = new BoneCPDataSource
  datasource.setJdbcUrl("jdbc:h2:mem:play")
  datasource.setUsername("sa")
  datasource.setPassword("")
  
  val driver = H2Driver
  val database = new Database("test", datasource, driver)
  val testDBAction = new DBAction(database)
  import driver.simple._
  
  class IDs(tag:Tag) extends Table[Int](tag, "IDS") {
    def id = column[Int]("id")
    def * = id
  }

  val Ids = TableQuery[IDs]
  
  "DBAction" should {
    "be instantiable without a Play Application" in {
      val ids = List(1, 2, 3)
      database.withSession { implicit session: Session =>
        Ids.ddl.create
        Ids.insertAll(ids: _*)
      }

      val listAction = testDBAction { implicit rs =>
        Ok(Ids.list.mkString(" "))
      }

      val result = listAction(FakeRequest())

      status(result) must equalTo(OK)
      contentAsString(result) must contain(ids.mkString(" "))
    }

    "provide an implicit Database-specific ExecutionContext" in {
      val expectedDBSpecificEC = testDBAction.attributes(database.name).executionContext.toString
      val notExpectedDefaultEC = scala.concurrent.ExecutionContext.Implicits.global.toString

      val ecAction = testDBAction { implicit rs =>
        def implicitECToString(implicit ec: ExecutionContext) = ec.toString
        Ok(implicitECToString)
      }

      val result = ecAction(FakeRequest())

      status(result) must equalTo(OK)
      contentAsString(result) must be_==(expectedDBSpecificEC)
      contentAsString(result) must be_!==(notExpectedDefaultEC)
    }
  }
}
