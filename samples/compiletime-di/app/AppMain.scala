package core

import controllers.{Assets, Application}
import dao.{CatDAO, DogDAO}
import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.db.slick.{DbName, SlickComponents}
import router.Routes
import slick.driver.JdbcProfile

class AppMain extends ApplicationLoader {
  def load(context: Context) = {
    Logger.configure(context.environment)
    val components = new ApplicationComponents(context)
    components.application
  }
}

class ApplicationComponents(context: Context) extends BuiltInComponentsFromContext(context)
  with SlickComponents with SlickEvolutionsComponents with EvolutionsComponents {

  lazy val dbConf = api.dbConfig[JdbcProfile](DbName("default"))
  lazy val myDbConf = api.dbConfig[JdbcProfile](DbName("mydb"))

  lazy val catDao = new CatDAO(dbConf.db)
  lazy val dogDao = new DogDAO(myDbConf.db)

  lazy val applicationController = new Application(catDao, dogDao)

  override lazy val router =
    new Routes(
      httpErrorHandler, applicationController, new Assets(httpErrorHandler)
    )

  // This is required by EvolutionsComponents
  lazy val dynamicEvolutions: DynamicEvolutions = new DynamicEvolutions

  def onStart() = {
    // applicationEvolutions is a val and requires evaluation
    applicationEvolutions
  }

  onStart()
}