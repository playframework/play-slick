# Play Slick Advanced Topics

## Connection Pool

With Slick 3 release, Slick starts and controls both a connection pool and a thread pool for optimal asynchronous execution of your database actions.

In Play Slick we have decided to let Slick be in control of creating and managing the connection pool (the default connection pool used by Slick 3 is [HikariCP](https://github.com/brettwooldridge/HikariCP)), which means that to tune the connection pool you will need to look at the Slick ScalaDoc for [Database.forConfig](https://scala-slick.org/doc/3.3.2/api/index.html#slick.jdbc.JdbcBackend$DatabaseFactoryDef@forConfig%28path:String,config:com.typesafe.config.Config,driver:java.sql.Driver,classLoader:ClassLoader%29:JdbcBackend.this.Database) (make sure to expand the `forConfig` row in the doc). In fact, be aware that any value you may pass for setting the Play connection pool (e.g., under the key `play.db.default.hikaricp`) is simply not picked up by Slick, and hence effectively ignored.

Also, note that as stated in the [Slick documentation](https://scala-slick.org/doc/3.3.2/database.html#connection-pools), a reasonable default for the connection pool size is calculated from the thread pool size. In fact, you should only need to tune `numThreads` and `queueSize` in most cases, for each of your database configurations.

Finally, it's worth mentioning that while Slick allows using a different connection pool than [HikariCP](https://github.com/brettwooldridge/HikariCP) (though, Slick currently only offers built-in support for HikariCP, and requires you to provide an implementation of [JdbcDataSourceFactory](https://scala-slick.org/doc/3.3.2/api/index.html#slick.jdbc.JdbcDataSourceFactory) if you want to use a different connection pool), Play Slick currently doesn't allow using a different connection pool than HikariCP.

> **Note**: Changing the value of `play.db.pool` won't affect what connection pool Slick is using. Furthermore, be aware that any configuration under `play.db` is not considered by Play Slick.

## Thread Pool

With Slick 3.0 release, Slick starts and controls both a thread pool and a connection pool for optimal asynchronous execution of your database actions.

For optimal execution, you may need to tune the `numThreads` and `queueSize` parameters, for each of your database configuration. Refer to the [Slick documentation](https://scala-slick.org/doc/3.3.2/database.html#database-thread-pool) for details.

Since `3.2.2` of Slick, there is a requirement that `maxConnections = maxThreads` otherwise you might see these messages in your logs:

 > [warn] s.u.AsyncExecutor - Having maxConnection > maxThreads can result in deadlocks if transactions or database locks are used.
 
To fix this, simply set them in your config. For example,

```
slick.dbs.default.profile="slick.profile.PostgresProfile$"
slick.dbs.default.db.dataSourceClass="slick.jdbc.DatabaseUrlDataSource"
slick.dbs.default.db.numThreads=20
slick.dbs.default.db.maxConnections=20
```
