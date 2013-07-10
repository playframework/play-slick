// This code is injected into a Slick package to hook into
// Slick's private API (which Slick should open up in the future).
// Since play-slick is developed in close cooperation with the
// Slick team this is better than reproducing Slick's Database
// API here (in a previously even incompatible way).
package scala.slick.session
abstract class PlayDatabase extends Database{
  protected def datasource : javax.sql.DataSource
  protected[session] def createConnection(): java.sql.Connection = datasource.getConnection
}
