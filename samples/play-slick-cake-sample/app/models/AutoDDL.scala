package play.api.db.slick
object AutoDDL extends AutoDDLInterface{
  import models.current.dao._

  def tables = Map(
  	"default" -> Seq(Cats)
  )
}