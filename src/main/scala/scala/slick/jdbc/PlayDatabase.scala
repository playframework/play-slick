//nothing gets injected now yay
package scala.slick.jdbc

//import scala.slick.jdbc.JdbcBackend

abstract class PlayDatabase extends JdbcBackend.Database  {
  protected def datasource: javax.sql.DataSource
  def createConnection(): java.sql.Connection = datasource.getConnection

}
