# Play Slick Migration Guide

This is a guide for migrating from Play Slick v0.8 to v1.0.

It assumes you have already migrated your project to use Play 2.4 (see [Play 2.4 Migration Guide]), that you have read the [Slick 3 documentation], and are ready to migrate your Play application to use the new Slick Database I/O Actions API.

[Play 2.4 Migration Guide]: https://www.playframework.com/documentation/2.4.x/Migration24)
[Slick 3 documentation]: http://slick.typesafe.com/docs/

## Build changes

Update the Play Slick dependency in your sbt build to match the version provided in the [[Setup|PlaySlick#Setup]] section.


## Database configuration

With the past releases of Slick Play (which used Slick 2.1 or earlier), you used to configure Slick datasources exactly like you would configure Play JDBC datasources. This is no longer the case, and the following configuration will now be ignored by Play Slick:

```conf
db.default.driver=org.h2.Driver
db.default.url="jdbc:h2:mem:play"
db.default.user=sa
db.default.password=""
```

There are several reasons for this change. First, the above is not a valid Slick configuration. Second, in Slick 3 you configure not just the datasource, but also both a connection pool and a thread pool. Therefore, it makes sense for Play Slick to use an entirely different path for configuring Slick databases. The default path for Slick configuration is now `slick.dbs`.

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

## `DBAction` and `DBSessionRequest` were removed

Play Slick used to provide a `DBAction` that was useful for:

* Conveniently pass the Slick `Session` into your Action method.
* Execute the action's body, and hence any blocking call to the database, in a separate thread pool.
* Limiting the number of blocking requests queued in the thread pool (useful to limit application's latency)

`DBAction` was indeed handy when using Slick 2.1. However, with the new Slick 3 release, we don't need it anymore. The reason is that Slick 3 comes with a new asynchronous API (a.k.a., Database I/O Actions API) that doesn't need the user to manipulate neither a `Session` nor a `Connection`. This makes `DBSessionRequest` and `DBAction`, together with its close friends `CurrentDBAction` and `PredicatedDBAction`, completely obsolete, which is why they have been removed.

Having said that, migrating your code should be as simple as changing all occurrences of `DBAction` and friends, with the standard Play `Action.async`. Click [[here|PlaySlick#Running-a-database-query-in-a-Controller]] for an example.

## Thread Pool

Play Slick used to provide a separate thread pool for executing controller's actions requiring to access a database. Slick 3 already does exactly this, hence there is no longer a need for Play Slick to create and manage additional thread pools. It follows that the below configuration parameter are effectively obsolete and should be removed from your **applications.conf**:

```conf
db.$dbName.maxQueriesPerRequest
slick.db.execution.context
```

The parameter `db.$dbName.maxQueriesPerRequest` was used to limit the number of tasks queued in the thread pool. In Slick 3 you can reach similar results by tuning the configuration parameters `numThreads` and `queueSize`. Read the Slick ScalaDoc for [Database.forConfig].

While the parameter `slick.db.execution.context` was used to name the thread pools created by Play Slick. In Slick 3, each thread pool is named using the Slick database configuration path, i.e., if in your **application.conf** you have provided a Slick configuration for the database named `default`, then Slick will create a thread pool named `default` for executing the database action on the default database. Note that the name used for the thread pool is not configurable.

[Database.forConfig]: http://slick.typesafe.com/doc/3.0.0-RC3/api/index.html#slick.jdbc.JdbcBackend$DatabaseFactoryDef@forConfig(String,Config,Driver):Database

## `Profile` was removed

The trait `Profile` was removed and you can use instead `HasDatabaseConfigProvider` or `HasDatabaseConfig` with similar results.

The trait to use depend on what approach you select to retrieve a Slick database and driver (i.e., an instance of `DatabaseConfig`). If you decide to use dependency injection, then `HasDatabaseConfigProvider` will serve you well. Otherwise, use `HasDatabaseConfig`.

Read [[here|PlaySlick#Usage]] for a discussion of how to use dependency injection vs global lookup to retrieve an instance of `DatabaseConfig`.

## `Database` was removed

The object `Database` was removed. To retrieve a Slick database and driver (i.e., an instance of `DatabaseConfig`) read [[here|PlaySlick#Usage]].

## `Config` was removed

The `Config` object, together with `SlickConfig` and `DefaultSlickConfig`, were removed. These abstractions are simply not needed. If you used to call `Config.driver` or `Config.datasource` to retrieve the Slick driver and database, you should now use `DatabaseConfigProvider`. Read [[here|PlaySlick#Usage]] for details.

## `SlickPlayIteratees` was removed

If you were using `SlickPlayIteratees.enumerateSlickQuery` to stream data from the database, you will be happy to know that doing so became a lot easier. Slick 3 implements the [reactive-streams] SPI, and Play 2.4 provides a utility class to handily convert a reactive stream into a Play enumerator.

In Slick, you can obtain a reactive stream by calling the method `stream` on a Slick database instance (instead of the eager `run`). To convert the stream into an enumerator simply call `play.api.libs.streams.Streams.publisherToEnumerator`, passing the stream in argument.

For a full example, have a look at [this sample projet](https://github.com/playframework/play-slick/tree/master/samples/iteratee).

[reactive-streams]: http://www.reactive-streams.org/

## DDL support was removed

Previous versions of Play Slick included a DDL plugin which would read your Slick tables definitions, and automatically creates schema updates on reload. While this is an interesting and useful feature, the underlying implementation was fragile, and and relied on the assumption that your tables would be accessible via a module (i.e., a Scala `object`). This coding pattern was possible because Play Slick allowed to import the Slick driver available via a top-level import. However, because support for [automatic detection of the Slick driver](#Automatic-Slick-driver-detection) was removed, you will not declare a top-level import for the Slick driver. This implies that Slick tables will no longer be accessible via a module. This fact breaks the assumption made in the initial implementation of the DDL plugin, and it's the reason why the feature was removed.

The consequence of the above is that you are in charge of creating and managing your project's database schema. Therefore, whenever you make a change a Slick table in the code, make sure to also update the database schema. If you find it tedious to manually keep in sync your database schema and the related table definition in your code, you may want to have a look at the code generation feature available in Slick.