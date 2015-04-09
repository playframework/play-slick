# Play Slick Advanced Topics

## Connection Pool

With Slick 3.0 release, Slick starts and controls both a connection pool and a thread pool for optimal asynchronous execution of your database actions.

It's worth noting that while Slick allows using a different connection pool than [HikariCP] (though, Slick currently only offers built-in support for HikariCP, and requires you to provide an implementation of [JdbcDataSourceFactory] if you want to use a different connection pool), Play Slick currently doesn't allow using a different connection pool than HikariCP. If you find yourself needing this feature, please drop us a note on the [playframework-dev] mailing list.

> Note: As stated in the [Slick documentation], a reasonable default for the connection pool size is calculated from the thread pool size. Therefore, you should only need to tune `numThreads` and `queueSize` in most cases, for each of your database configuration.

> Note: Changing the value of `play.db.pool` won't affect what connection pool Slick is using. In fact, it's worth noting that Play Slick disables both the `play.api.db.DBModule` and `play.api.db.HikariCPModule` Play modules, and it will let Slick be in control of creating and managing the connection pool.

## Thread Pool

With Slick 3.0 release, Slick starts and controls both a thread pool and a connection pool for optimal asynchronous execution of your database actions.

For optimal execution, you may need to tune the `numThreads` and `queueSize` parameters, for each of your database configuration. Refer to the [Slick documentation] for details.


[playframework-dev]: https://groups.google.com/forum/#!forum/play-framework-dev 
[Slick documentation]: http://slick.typesafe.com/docs
[HikariCP]: http://brettwooldridge.github.io/HikariCP/
[JdbcDataSourceFactory]: http://slick.typesafe.com/doc/3.0.0-RC3/api/index.html#slick.jdbc.JdbcDataSourceFactory