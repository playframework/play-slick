package test

import dao.CatDAO
import models.Cat
import org.specs2.mutable.Specification
import test.TestEnvironment.WithApplicationComponents

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/** test the kitty cat database */
class CatDAOSpec extends Specification {

  "CatDAO" should {
    "work as expected" in new WithApplicationComponents {
      val dao: CatDAO = appComponents.catDao

      val testKitties = Set(
        Cat("kit", "black"),
        Cat("garfield", "orange"),
        Cat("creme puff", "grey"))

      Await.result(Future.sequence(testKitties.map(dao.insert)), 1 seconds)
      val storedCats = Await.result(dao.all(), 1 seconds)

      storedCats.toSet must equalTo(testKitties)
    }
  }
}
