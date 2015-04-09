package test

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import org.specs2.mutable.Specification

import dao.CatDAO
import models.Cat
import play.api.test.WithApplicationLoader

/** test the kitty cat database */
class CatDAOSpec extends Specification {

  "CatDAO" should {
    "work as expected" in new WithApplicationLoader {
      val dao = new CatDAO

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
