package play.api.db.slick

import org.specs2.mutable.Specification
import scala.concurrent.ExecutionContext

/**
 * Provides an antidote to ConcurrentExecutionContext's implicit
 * ExecutionContext.
 *
 * See: https://groups.google.com/d/msg/specs2-users/m5nn4nSNu0Q/aw4sb7ha_LwJ
 */
abstract class SpecificationWithoutExecutionContext extends Specification {
  // Override value in order to remove the implicit modifier
  override val concurrentExecutionContext = ExecutionContext.global
}