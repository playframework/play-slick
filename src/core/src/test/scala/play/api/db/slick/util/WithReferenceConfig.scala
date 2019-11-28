package play.api.db.slick.util

import org.specs2.specification.Scope

import play.api.Configuration

trait WithReferenceConfig extends Scope {
  val ref = Configuration.reference
  def enabledModules(c: Configuration): List[String] = {
    ref.get[Seq[String]]("play.modules.enabled").toList
  }
}
