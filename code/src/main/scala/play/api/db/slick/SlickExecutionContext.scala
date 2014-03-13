package play.api.db.slick

import play.api.libs.concurrent.Akka
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import play.api.Application
import play.core.NamedThreadFactory
import java.util.concurrent._

object SlickExecutionContext {
  def threadPoolExecutionContext(minConnections: Int, maxConnections: Int) = {
    val tpe = new ThreadPoolExecutor(minConnections, maxConnections,
      0L, TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue[Runnable](),
      new NamedThreadFactory("slick.db.execution.context"))
    ExecutionContext.fromExecutorService(tpe) -> tpe
  }

}