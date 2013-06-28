package scala.slick.session
import java.sql.Connection
import javax.sql.{DataSource}
// This hooks into Slick's private API (which Slick should open up in the future).
// Since play-slick is developed in close cooperation with the
// Slick team this is better than reproducing Slick's Database
// API here (in a previously even incompatible way).
abstract class PlayDatabase extends Database{
  protected def datasource : DataSource
  protected[session] def createConnection(): Connection = datasource.getConnection
}