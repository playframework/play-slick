package play.api.db.slick

import play.api.libs.concurrent.Akka

object SlickExecutionContexts {

  lazy val executionContext = {
    val app = play.api.Play.current
    val configSection = "akka.actor.slick-context"
    app.configuration.getConfig(configSection) match { //TODO: create a better default execution context
      case Some(_) => Akka.system(app).dispatchers.lookup(configSection)
      case None => play.api.libs.concurrent.Execution.defaultContext
    }
  }

}