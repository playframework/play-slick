package play.api.db.slick.internal

import java.sql.Connection

import javax.inject.Inject
import javax.sql.DataSource
import play.api.db.DBApi
import play.api.db.{Database => PlayDatabase}
import play.api.db.slick.Name
import play.api.db.slick.SlickApi
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

private[slick] class DBApiAdapter @Inject() (slickApi: SlickApi) extends DBApi {
  private lazy val databasesByName: Map[Name, PlayDatabase] = slickApi.dbConfigs[JdbcProfile]().map {
    case (name, dbConfig) => (name, new DBApiAdapter.DatabaseAdapter(name, dbConfig))
  }(collection.breakOut)

  override def databases: Seq[PlayDatabase] = databasesByName.values.toSeq

  def database(name: String): PlayDatabase =
    databasesByName.getOrElse(Name(name), throw new IllegalArgumentException(s"Could not find database for $name"))

  def shutdown(): Unit = {
    // no-op: shutting down dbs is automatically managed by `slickApi`
    ()
  }
}

private[slick] object DBApiAdapter {
  private class DatabaseAdapter(_name: Name, dbConfig: DatabaseConfig[JdbcProfile]) extends PlayDatabase {
    def name: String = _name.value
    def dataSource: DataSource = throw new UnsupportedOperationException
    def url: String = dbConfig.db.createSession().metaData.getURL
    def getConnection(): Connection = {
      val session = dbConfig.db.createSession()
      session.conn
    }
    def getConnection(autocommit: Boolean): Connection = getConnection() // FIXME: auto-commit is ignored (in slick, I believe auto-commit is on by default, but need to double check)
    def withConnection[A](block: Connection => A): A = {
      dbConfig.db.withSession { session =>
        val conn = session.conn
        block(conn)
      }
    }
    def withConnection[A](autocommit: Boolean)(block: Connection => A): A = withConnection(block) // FIXME: auto-commit is ignored (in slick, I believe auto-commit is on by default, but need to double check)
    def withTransaction[A](block: Connection => A): A = {
      dbConfig.db.withTransaction { session =>
        val conn = session.conn
        block(conn)
      }
    }
    def shutdown(): Unit = () // no-op
  }
}