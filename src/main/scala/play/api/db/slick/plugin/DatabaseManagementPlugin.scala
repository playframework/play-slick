package play.api.db.slick.plugin

import play.api.Plugin
import play.api.db.slick.Database
import play.api.Application

class DatabaseManagementPlugin(app: Application) extends Plugin {
  override def enabled = true

  override def onStart(): Unit = {
    Database.cachedDatabases.clear() //clear resident databases
  }
}