import scala.slick.lifted.MappedTypeMapper
import java.text.SimpleDateFormat

package object models {
  implicit val javaUtilDateTypeMapper = MappedTypeMapper.base[java.util.Date, java.sql.Date](
    x => new java.sql.Date(x.getTime),
    x => new java.util.Date(x.getTime))

}