package play.api.db.slick.evolutions.internal

import java.sql.Connection

import javax.inject.Inject
import javax.sql.DataSource
import play.api.Logger
import play.api.db.slick.DbName
import play.api.db.slick.IssueTracker
import play.api.db.slick.SlickApi
import play.api.db.DBApi
import play.api.db.TransactionIsolationLevel
import play.api.db.{ Database => PlayDatabase }
import slick.basic.DatabaseConfig
import slick.jdbc.DataSourceJdbcDataSource
import slick.jdbc.JdbcProfile
import slick.jdbc.hikaricp.HikariCPJdbcDataSource

import scala.util.control.ControlThrowable

private[evolutions] class DBApiAdapter @Inject() (slickApi: SlickApi) extends DBApi {
  private lazy val databasesByName: Map[DbName, PlayDatabase] = slickApi
    .dbConfigs[JdbcProfile]()
    .map {
      case (name, dbConfig) => (name, new DBApiAdapter.DatabaseAdapter(name, dbConfig))
    }
    .toMap

  override def databases: Seq[PlayDatabase] = databasesByName.values.toSeq

  def database(name: String): PlayDatabase =
    databasesByName.getOrElse(DbName(name), throw new IllegalArgumentException(s"Could not find database for $name"))

  def shutdown(): Unit = {
    // no-op: shutting down dbs is automatically managed by `slickApi`
    ()
  }
}

private[evolutions] object DBApiAdapter {
  // I don't really like this adapter as it can be used as a trojan horse. Let's keep things simple for the moment,
  // but in the future we may need to become more defensive and provide custom implementation for `java.sql.Connection`
  // and `java.sql.DataSource` to prevent the ability of closing a database connection or database when using this
  // adapter class.
  private class DatabaseAdapter(_name: DbName, dbConfig: DatabaseConfig[JdbcProfile]) extends PlayDatabase {
    private val logger = Logger(classOf[DatabaseAdapter])

    def name: String = _name.value

    def dataSource: DataSource = {
      dbConfig.db.source match {
        case ds: DataSourceJdbcDataSource => ds.ds
        case hds: HikariCPJdbcDataSource  => hds.ds
        case other =>
          logger.error(s"Unexpected data source type ${other.getClass}. Please, file a ticket $IssueTracker.")
          throw new UnsupportedOperationException
      }
    }

    lazy val url: String = withConnection(_.getMetaData.getURL())

    override def getConnection(): Connection = getConnection(autocommit = true)

    override def getConnection(autocommit: Boolean): Connection = {
      val connection = dbConfig.db.source.createConnection()
      try {
        connection.setAutoCommit(autocommit)
      } catch {
        case e: Throwable =>
          // setAutoCommit can fail, so we need to close the connection
          // which usually means we are returning it back to the pool and
          // avoiding a leak.
          connection.close()
          throw e
      }
      connection
    }

    def withConnection[A](block: Connection => A): A = {
      withConnection(autocommit = true)(block)
    }

    def withConnection[A](autocommit: Boolean)(block: Connection => A): A = {
      val connection = getConnection(autocommit)
      try {
        block(connection)
      } finally {
        connection.close()
      }
    }

    def withTransaction[A](block: Connection => A): A = {
      withConnection(autocommit = false) { connection =>
        try {
          val r = block(connection)
          connection.commit()
          r
        } catch {
          case e: ControlThrowable =>
            connection.commit()
            throw e
          case e: Throwable =>
            connection.rollback()
            throw e
        }
      }
    }

    override def withTransaction[A](isolationLevel: TransactionIsolationLevel)(block: Connection => A): A = {
      withConnection(autocommit = false) { connection =>
        val oldIsolationLevel = connection.getTransactionIsolation
        try {
          connection.setTransactionIsolation(isolationLevel.id)
          val r = block(connection)
          connection.commit()
          r
        } catch {
          case e: ControlThrowable =>
            connection.commit()
            throw e
          case e: Throwable =>
            connection.rollback()
            throw e
        } finally {
          connection.setTransactionIsolation(oldIsolationLevel)
        }
      }
    }

    def shutdown(): Unit = {
      // no-op. The rationale is that play-slick already takes care of closing the database on application shutdown
      ()
    }
  }
}
