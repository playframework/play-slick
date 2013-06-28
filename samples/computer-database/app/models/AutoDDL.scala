package play.api.db.slick
object AutoDDL extends AutoDDLInterface{
  import models._
  def tables = Map(
  	"default" -> Seq(Companies,Computers)
  )
}