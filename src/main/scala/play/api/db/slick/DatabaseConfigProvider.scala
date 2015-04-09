package play.api.db.slick

import slick.profile.BasicProfile
import slick.backend.DatabaseConfig

trait DatabaseConfigProvider {
  def get[P <: BasicProfile]: DatabaseConfig[P]
}

object DatabaseConfigProvider {
  import play.api.Application
  import play.api.Configuration
  private object DatabaseConfigLocator {

    private val slickApiCache = Application.instanceCache[SlickApi]
    private def slickApi(implicit app: Application): SlickApi = slickApiCache(app)

    private val configurationCache = Application.instanceCache[Configuration]
    private def configuration(implicit app: Application): Configuration = configurationCache(app)

    def apply[P <: BasicProfile](implicit app: Application): DatabaseConfig[P] = {
      val defaultDbName = configuration.underlying.getString(SlickModule.DefaultDbName)
      this(defaultDbName)
    }

    def apply[P <: BasicProfile](dbName: String)(implicit app: Application): DatabaseConfig[P] =
      slickApi.dbConfig[P](dbName)
  }

  def get[P <: BasicProfile](implicit app: Application): DatabaseConfig[P] =
    DatabaseConfigLocator(app)

  def get[P <: BasicProfile](dbName: String)(implicit app: Application): DatabaseConfig[P] =
    DatabaseConfigLocator(dbName)
}