package play.api.db.slick

import play.api._
import play.api.Application
import scala.slick.session.Session
import scala.slick.session.Database
import play.api.db.{ DB => PlayDB }
import java.util.concurrent.ConcurrentHashMap


/**
 * Helper object to access Databases using Slick
 */
object DB extends DB {
  def apply(name: String) = {
    new DB {
      override lazy val CurrentDB = name
    }
  }
}

trait DB {

  import play.api.Play.current

  import play.api.db.{ DB => PlayDB }

  private lazy val dbMap = new ConcurrentHashMap[String, Database]()

  def database(name: String)(implicit app: Application): Database = {
    Option(dbMap.get(name)).getOrElse {
      if (app.configuration.getConfig(s"db.$name").isEmpty) app.configuration.reportError(s"db.$name", s"While loading datasource: could not find db.$name in configuration", None)
      val db = Database.forDataSource(PlayDB.getDataSource(name)(app)) //FIXME: when running test multiple times I get the errors printed below- I think this is the guilty line...
/*
[error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 10. Exception: null
[error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 9. Exception: null
[error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 8. Exception: null
[error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 7. Exception: null
[error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 6. Exception: null
[error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 5. Exception: null
[error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 4. Exception: null
[error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 3. Exception: null
[error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 2. Exception: null
[error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 1. Exception: null
[error] c.j.b.ConnectionHandle - Database access problem. Killing off all remaining connections in the connection pool. SQL State = 08001
[error] c.j.b.PoolWatchThread - Error in trying to obtain a connection. Retrying in 1000ms
java.sql.SQLException: No suitable driver found for jdbc:h2:mem:play-test--205858406
  at java.sql.DriverManager.getConnection(DriverManager.java:602) ~[na:1.6.0_33]
  at java.sql.DriverManager.getConnection(DriverManager.java:185) ~[na:1.6.0_33]
  at com.jolbox.bonecp.BoneCP.obtainRawInternalConnection(BoneCP.java:256) ~[bonecp-0.7.1.RELEASE.jar:0.7.1.RELEASE]
  at com.jolbox.bonecp.ConnectionHandle.obtainInternalConnection(ConnectionHandle.java:211) ~[bonecp-0.7.1.RELEASE.jar:0.7.1.RELEASE]
  at com.jolbox.bonecp.ConnectionHandle.<init>(ConnectionHandle.java:170) ~[bonecp-0.7.1.RELEASE.jar:0.7.1.RELEASE]
  at com.jolbox.bonecp.PoolWatchThread.fillConnections(PoolWatchThread.java:101) [bonecp-0.7.1.RELEASE.jar:0.7.1.RELEASE]
 */
      if (db == null) throw app.configuration.reportError(s"db.$name", s"While loading datasource: could not create database named: $name", None)
      dbMap.put(name, db)
      db
    }
  }

  def withSession[A](name: String)(block: Session => A): A = {
    database(name).withSession { session: Session =>
      block(session)
    }
  }

  def withTransaction[A](name: String)(block: Session => A): A = {
    database(name).withTransaction { session: Session =>
      block(session)
    }
  }

  protected lazy val CurrentDB = {
    Play.application.mode match {
      case Mode.Test if(Play.application.configuration.getString("db.test.url").isDefined) => "test"
      case _ => "default"
    }
  }

  def database: Database = {
    database(CurrentDB)
  }

  def withSession[A](block: Session => A): A = {
    database.withSession { session: Session =>
      block(session)
    }
  }

  def withTransaction[A](block: Session => A): A = {
    database.withTransaction { session: Session =>
      block(session)
    }
  }

}
