# Play Slick 0.9 Migration Guide

This is a guide for migrating from Play Slick 0.8 to Play Slick 0.9.

It assumes you have already migrated your project to use Play 2.4 (see [Play 2.4 Migration Guide]), that you have read the [Slick 3 documentation], and are ready to migrate your Play application to use the new Slick Database I/O Actions API.

## Build changes

Update the Play Slick dependency in your sbt build to `0.9.0`:

```sbt
"com.typesafe.play" %% "play-slick" % "0.9.0"
```

[Play 2.4 Migration Guide]: https://www.playframework.com/documentation/2.4.x/Migration24)
[Slick 3 documentation]: http://slick.typesafe.com/docs/

## Database configuration

With the past releases of Slick Play (which used Slick 2.1 or earlier), you used to configure Slick datasources exactly like you would configure Play JDBC datasources. This is no longer the case, and the following configuration will in fact be ignored by this new release of the Play Slick module:

```conf
db.default.driver=org.h2.Driver
db.default.url="jdbc:h2:mem:play"
db.default.user=sa
db.default.password=""
```

There are several reasons for this change. First, the above is (and was) not a valid Slick configuration. Second, in Slick 3 you configure not just the datasource, but also both a connection pool and a thread pool. Therefore, it makes sense for Play Slick to use an entirely different path for configuring Slick databases. The default path for Slick configuration is now `slick.dbs`.

Here is how you would need to migrate the above configuration:

```conf
slick.dbs.default.driver=slick.driver.H2Driver$ # You must provide the required Slick driver! 
slick.dbs.default.db.driver=org.h2.Driver
slick.dbs.default.db.url="jdbc:h2:mem:play"
slick.dbs.default.db.user=sa
slick.dbs.default.db.password=""
```

> Note: If your database configuration contains settings for the connection pool, be aware that you will need to migrate those settings as well. However, this may be a bit trickier, because Play 2.3 default connection pool used to be BoneCP, while the default Slick 3 connection pool is HikariCP. Read [[here|PlaySlickAdvancedTopics#Connection-Pool]] for more details.

## Automatic Slick driver detection

Play Slick used to automatically infer the needed Slick driver from the datasource configuration. This feature was removed, hence you must provide the Slick driver to use, for each Slick database configuration, in your **application.conf**.

The rationale for removing this admittedly handy feature is that we want a Play Slick configuration to be a valid Slick configuration. Furthermore, it's not always possible to automatically detect the correct Slick driver from the database configuration (if this was possible, then Slick would already provide such functionality).

Therefore, you will need to make the following changes:

  * Each of your Slick database configuration must provide the Slick driver (see [here](#Database-configuration) for an example of how to migrate your database configuration).
  * Remove all imports to `import play.api.db.slick.Config.driver.simple._`.
  * Read [[here|PlaySlick#Usage]] for how to lookup the Slick driver and database instances (which are needed to use the new Slick 3 Database I/O Actions API).

## DBAction was removed

Play Slick used to provide a `DBAction` that was useful for:

* Conveniently pass the Slick `Session` into your Action method.
* Execute the action's body, and hence any blocking call to the database, in a separate thread pool.
* Limiting the number of blocking requests queued in the thread pool (useful to limit application's latency)

`DBAction` was indeed handy when using Slick 2.1. However, with the new Slick 3 release, we don't need it anymore. The reason is that Slick 3 comes with a new asynchronous API (a.k.a., Database I/O Actions API) that doesn't need the user to manipulate neither a `Session` nor a `Connection`. This makes `DBAction`, and its close friends `CurrentDBAction` and `PredicatedDBAction`, completely obsolete, which is why they have been removed.

Having said that, migrating your code should be as simple as changing all occurrences of `DBAction` and friends, with the standard Play `Action.async`. Click [[here|PlaySlick#Usage]] for an example.

## Thread Pool

Play Slick used to provide a separate thread pool for executing controller's actions requiring to access a database. Slick 3 already does exactly this, hence there is no longer a need for Play Slick to create and manage additional thread pools. It follows that the below configuration parameter are effectively obsolete and should be removed from your **applications.conf**:

```conf
db.$dbName.maxQueriesPerRequest
slick.db.execution.context
```

The parameter `db.$dbName.maxQueriesPerRequest` was used to limit the number of tasks queued in the thread pool. In Slick 3 you can reach similar results by tuning the configuration parameters `numThreads` and `queueSize`. Read the Slick ScalaDoc for [Database.forConfig].

While the parameter `slick.db.execution.context` was used to name the thread pools created by Play Slick. In Slick 3, each thread pool is named using the Slick database configuration path, i.e., if in your **application.conf** you have provided a Slick configuration for the database named `default`, then Slick will create a thread pool named `default` for executing the database action on the default database. Note that the name used for the thread pool is not configurable.

[Database.forConfig]: http://slick.typesafe.com/doc/3.0.0-RC3/api/index.html#slick.jdbc.JdbcBackend$DatabaseFactoryDef@forConfig(String,Config,Driver):Database