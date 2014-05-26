package play.api.db.slick

import org.specs2.matcher._
import org.specs2.control._

import org.specs2._
import org.specs2.time._
import org.specs2.execute._
import org.specs2.main.ArgumentsShortcuts
import org.specs2.specification._
import org.specs2.mutable._
import org.specs2.mutable.FormattingFragments


/**
 * Needed because 'FutureMatchers' bring their own ExecutionContext which kills 'TestableDBActionTest'
 */
abstract class SpecificationWithoutFutureMatchers extends SpecificationLike
trait SpecificationLike extends SpecificationStructure with SpecificationFeatures {
  def is = fragments
}

trait SpecificationFeatures extends SpecificationStringContext
  with mutable.FragmentsBuilder
  with mutable.SpecificationInclusion
  with ArgumentsArgs
  with ArgumentsShortcuts
  with MustThrownMatchers
  with ShouldThrownMatchers
  with FormattingFragments
  with StandardResults
  with StandardMatchResults
  with mutable.Tags
  with TimeConversions
  with PendingUntilFixed
  with Contexts
  with SpecificationNavigation
  with ContextsInjection
  with Debug


trait Matchers extends AnyMatchers
  with TraversableMatchers
  with MapMatchers
  with StringMatchers
  with ExceptionMatchers
  with NumericMatchers
  with OptionMatchers
  with EitherMatchers
  with TryMatchers
  with EventuallyMatchers
  with MatchersImplicits
  with LanguageFeatures

object Matchers extends Matchers

trait MustMatchers extends Matchers with MustExpectations
object MustMatchers extends MustMatchers
trait ShouldMatchers extends Matchers with ShouldExpectations
object ShouldMatchers extends ShouldMatchers

trait MustThrownMatchers extends Matchers with MustThrownExpectations
object MustThrownMatchers extends MustThrownMatchers

trait ShouldThrownMatchers extends Matchers with ShouldThrownExpectations
object ShouldThrownMatchers extends ShouldThrownMatchers
