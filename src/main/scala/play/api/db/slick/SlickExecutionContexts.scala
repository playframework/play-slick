package play.api.db.slick

import play.api.libs.concurrent.Akka
import akka.actor.ActorSystem

object SlickExecutionContexts {

  lazy val executionContext = {
    val app = play.api.Play.current
    val configSection = "play.akka.actor.slick-context"
    app.configuration.getConfig(configSection) match {
      case Some(_) => Akka.system(app).dispatchers.lookup(configSection)
      case None =>
        //I could not for the life of me get Play's Actor system to load play-slick's application.conf so this is the solution I ended up with:
        ActorSystem("default-play-slick-system").dispatchers.lookup(configSection)
    }
  }

}