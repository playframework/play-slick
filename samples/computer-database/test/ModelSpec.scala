package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.db.slick.DB
import slick.session.Session
import play.api.Play.current

class ModelSpec extends Specification {
  
  import models._

  // -- Date helpers
  
  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str
  
  // --
  
  "Computer model" should {
    
    "be retrieved by id" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        DB.withSession{ implicit s:Session =>
          val Some(macintosh) = Computers.findById(21)
      
          macintosh.name must equalTo("Macintosh")
          macintosh.introduced must beSome.which(dateIs(_, "1984-01-24"))  
        }        
      }
    }
    
    "be listed along its companies" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        DB.withSession{ implicit s:Session =>
          val computers = Computers.list()
          computers.total must equalTo(574)
          computers.items must have length(10)
        }
      }
    }
    
    "be updated if needed" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        DB.withSession{ implicit s:Session =>
          Computers.update(21, Computer(name="The Macintosh", introduced=None, discontinued=None, companyId=Some(1)))
        
          val Some(macintosh) = Computers.findById(21)
          macintosh.name must equalTo("The Macintosh")
          macintosh.introduced must beNone
        }
      }
    }
    
  }
  
}