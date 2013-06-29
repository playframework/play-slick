package play.api.db.slick

import play.api.libs.concurrent.Akka
import play.api.Play.current

object SlickExecutionContext {
  val executionContext = Akka.system.dispatchers.lookup("akka.actor.slick-context")
}