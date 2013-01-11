package play.api.db.slick

import play.api.Plugin
import play.api.Application
import scala.slick.session.Session
import scala.slick.session.Database

/**
 * Helper object to access Databases using Slick
 */
object DB {
  import play.api.db.{ DB => PlayDB }
 
  def database(name: String)(implicit app: Application) = {
    if (app.configuration.getConfig(s"db.$name").isEmpty) app.configuration.reportError(s"db.$name", s"While loading datasource: could not find db.$name in configuration", None) 
    Database.forDataSource(PlayDB.getDataSource(name)(app))
  }
  
  def withSession[A](name: String)(block: Session => A)(implicit app: Application): A = {
    database(name).withSession { session: Session =>
      block(session)
    }
  }

  private val DefaultDB = "default"
  
  def withSession[A](block: Session => A)(implicit app: Application): A = {
    withSession(DefaultDB)(block)
  }

  def withTransaction[A](name: String)(block: Session => A)(implicit app: Application): A = {
    database(name).withTransaction { session: Session =>
      block(session)
    }
  }
  
  def withTransaction[A](block: Session => A)(implicit app: Application): A = {
    withTransaction(DefaultDB)(block)
  }

}
