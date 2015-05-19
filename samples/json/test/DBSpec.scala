package test

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import models.Cat
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.test.PlaySpecification
import play.api.test.WithApplication
import slick.driver.JdbcProfile
import tables.CatTable

/**
 * test the kitty cat database
 */
class DBSpec extends PlaySpecification {

  trait WithDatabaseConfig {
    lazy val (driver, db) = {
      val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
      (dbConfig.driver, dbConfig.db)
    }
  }

  "DatabaseConfigProvider" should {
    "work as expected" in new WithApplication with CatTable with WithDatabaseConfig {

      import driver.api._
      //create an instance of the table
      val Cats = TableQuery[Cats] //see a way to architect your app in the computers-database-slick sample

      val testKitties = Seq(
        Cat("kit", "black"),
        Cat("garfield", "orange"),
        Cat("creme puff", "grey"))

      val list = Await.result(for {
        _ <- db.run(Cats ++= testKitties)
        res <- db.run(Cats.result)
      } yield res, 1 seconds)

      list must equalTo(testKitties)
    }
  }
}
