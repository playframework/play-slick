package play.api.db.slick.util

import scala.collection.JavaConverters.asScalaBufferConverter

import org.specs2.specification.Scope

import play.api.Configuration

trait WithReferenceConfig extends Scope {
  val ref = Configuration.reference
  def enabledModules(c: Configuration): List[String] = {
    import scala.collection.JavaConverters._
    ref.getStringList("play.modules.enabled") match {
      case None        => Nil
      case Some(jlist) => jlist.asScala.toList
    }
  }
}