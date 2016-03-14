# Play Slick Advanced Topics

## Connection Pool

With Slick 3 release, Slick starts and controls both a connection pool and a thread pool for optimal asynchronous execution of your database actions.

In Play Slick we have decided to let Slick be in control of creating and managing the connection pool (the default connection pool used by Slick 3 is [HikariCP](http://brettwooldridge.github.io/HikariCP/)), which means that to tune the connection pool you will need to look at the Slick ScalaDoc for [Database.forConfig](http://slick.typesafe.com/doc/3.1.0/api/index.html#slick.jdbc.JdbcBackend$DatabaseFactoryDef@forConfig(String,Config,Driver,ClassLoader):Database) (make sure to expand the `forConfig` row in the doc). In fact, be aware that any value you may pass for setting the Play connection pool (e.g., under the key `play.db.default.hikaricp`) is simply not picked up by Slick, and hence effectively ignored.

Also, note that as stated in the [Slick documentation](http://slick.typesafe.com/doc/3.1.0/database.html#connection-pools), a reasonable default for the connection pool size is calculated from the thread pool size. In fact, you should only need to tune `numThreads` and `queueSize` in most cases, for each of your database configuration.

Finally, it's worth mentioning that while Slick allows using a different connection pool than [HikariCP](http://brettwooldridge.github.io/HikariCP/) (though, Slick currently only offers built-in support for HikariCP, and requires you to provide an implementation of [JdbcDataSourceFactory](http://slick.typesafe.com/doc/3.1.0/api/index.html#slick.jdbc.JdbcDataSourceFactory) if you want to use a different connection pool), Play Slick currently doesn't allow using a different connection pool than HikariCP.

> **Note**: Changing the value of `play.db.pool` won't affect what connection pool Slick is using. Furthermore, be aware that any configuration under `play.db` is not considered by Play Slick.

## Thread Pool

With Slick 3.0 release, Slick starts and controls both a thread pool and a connection pool for optimal asynchronous execution of your database actions.

For optimal execution, you may need to tune the `numThreads` and `queueSize` parameters, for each of your database configuration. Refer to the [Slick documentation](http://slick.typesafe.com/doc/3.1.0/database.html#database-thread-pool) for details.
