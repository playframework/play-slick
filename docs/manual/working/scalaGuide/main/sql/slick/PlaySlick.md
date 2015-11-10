# Using Play Slick

The Play Slick module makes [Slick](http://slick.typesafe.com/) a first-class citizen of Play.

The Play Slick module consists of two features:

  - Integration of Slick into Play's application lifecycle.
  - Support for [[Play database evolutions|Evolutions]].

Play Slick currently supports Slick 3.1 with Play 2.4, for both Scala 2.10 and 2.11.

> Note: This guide assumes you already know both Play 2.4 and Slick 3.1.

### Getting Help

If you are having trouble using Play Slick, check if the [[FAQ|PlaySlickFAQ]] contains the answer. Otherwise, feel free to reach out to [play-framework user group]. Also, note that if you are seeking help on Slick, the [slick user group] may be a better place.

Finally, if you prefer to get an answer for your Play and Slick questions in a timely manner, and with a well-defined SLA, you may prefer [to get in touch with Typesafe](http://www.typesafe.com/subscription), as it offers commercial support for these technologies.

[play-framework user group]: https://groups.google.com/forum/#!forum/play-framework
[slick user group]: https://groups.google.com/forum/#!forum/scalaquery

## About this release

If you have been using a previous version of Play Slick, you will notice that there have been quite a few major changes. It's recommended to read the [[migration guide|PlaySlickMigrationGuide]] for a smooth upgrade.

While, if this is the first time you are using Play Slick, you will appreciate that the integration of Slick in Play is quite austere. Meaning that if you know both Play and Slick, using Play Slick module should be straightforward.

## Setup

Add a library dependency on play-slick:

```scala
"com.typesafe.play" %% "play-slick" % "1.1.1"
```

The above dependency will also bring along the Slick library as a transitive dependency. This implies you don't need to add an explicit dependency on Slick, but you might still do so if needed. A likely reason for wanting to explicitly define a dependency to Slick is if you want to use a newer version than the one bundled with play-slick. Because Slick trailing dot releases are binary compatible, you won't incur any risk in using a different Slick trailing point release than the one that was used to build play-slick.

### Support for Play database evolutions

Play Slick supports [[Play database evolutions|Evolutions]].

To enable evolutions, you will need the following dependencies:

```scala
"com.typesafe.play" %% "play-slick" % "1.1.1"
"com.typesafe.play" %% "play-slick-evolutions" % "1.1.1"
```

Note there is no need to add the Play `evolutions` component to your dependencies, as it is a transitive dependency of the `play-slick-evolutions` module.

### JDBC driver dependency

Play Slick module does not bundle any JDBC driver. Hence, you will need to explicitly add the JDBC driver(s) you want to use in your application. For instance, if you would like to use an in-memory database such as H2, you will have to add a dependency to it:

```
"com.h2database" % "h2" % "${H2_VERSION}" // replace `${H2_VERSION}` with an actual version number
```

## Database Configuration

To have Play Slick module handling the lifecycle of Slick databases, it is important that you never create database's instances explicitly in your code. Rather, you should provide a valid Slick driver and database configuration in your **application.conf** (by convention the default Slick database must be called `default`):

```conf
# Default database configuration
slick.dbs.default.driver="slick.driver.H2Driver$"
slick.dbs.default.db.driver="org.h2.Driver"
slick.dbs.default.db.url="jdbc:h2:mem:play"
```

First, note that the above is a valid Slick configuration (for the complete list of configuration parameters that you can use to configure a database see the Slick ScalaDoc for [Database.forConfig] - make sure to expand the `forConfig` row in the doc).

Second, the `slick.dbs` prefix before the database's name is configurable. In fact, you may change it by overriding the value of the configuration key `play.slick.db.config`.

Third, in the above configuration `slick.dbs.default.driver` is used to configure the Slick driver, while `slick.dbs.default.db.driver` is the underlying JDBC driver used by Slick's backend. In the above configuration we are configuring Slick to use H2 database, but Slick supports several other databases. Check the [Slick documentation] for a complete list of supported databases, and to find a matching Slick driver.

Slick does not support the `DATABASE_URL` environment variable in the same way as the default Play JBDC connection pool. But starting in version 3.0.3, Slick provides a `DatabaseUrlDataSource` specifically for parsing the environment variable.

```conf
slick.dbs.default.driver="slick.driver.PostgresDriver$"
slick.dbs.default.db.dataSourceClass = "slick.jdbc.DatabaseUrlDataSource"
slick.dbs.default.db.properties.driver = "org.postgresql.Driver"
```

On some platforms, such as Heroku, you may substitue the `JDBC_DATABASE_URL`, which is in the format `jdbc:vendor://host:port/db?args`, if it is available. For example:

```conf
slick.dbs.default.driver="slick.driver.PostgresDriver$"
slick.dbs.default.db.driver="org.postgresql.Driver"
slick.dbs.default.db.url=${JDBC_DATABASE_URL}
```

>> Note: Failing to provide a valid value for both `slick.dbs.default.driver` and `slick.dbs.default.db.driver` will lead to an exception when trying to run your Play application.

[Slick documentation]: http://slick.typesafe.com/docs
[Database.forConfig]: http://slick.typesafe.com/doc/3.1.0/api/index.html#slick.jdbc.JdbcBackend$DatabaseFactoryDef@forConfig(String,Config,Driver,ClassLoader):Database

To configure several databases:

```conf
# Orders database
slick.dbs.orders.driver="slick.driver.H2Driver$"
slick.dbs.orders.db.driver="org.h2.Driver"
slick.dbs.orders.db.url="jdbc:h2:mem:play"

# Customers database
slick.dbs.customers.driver="slick.driver.H2Driver$"
slick.dbs.customers.db.driver="org.h2.Driver"
slick.dbs.customers.db.url="jdbc:h2:mem:play"
```

If something isn't properly configured, you will be notified in your browser:

[[images/database-config-error.png]]

> Note: Your application will be started only if you provide a valid Slick configuration.

## Usage

After having properly configured a Slick database, you can obtain a `DatabaseConfig` (which is a Slick type bundling a database and driver) in two different ways. Either by using dependency injection, or through a global lookup via the `DatabaseConfigProvider` singleton.

> Note: A Slick database instance manages a thread pool and a connection pool. In general, you should not need to shut down a database explicitly in your code (by calling its `close` method), as the Play Slick module takes care of this already.

### DatabaseConfig via Dependency Injection

Here is an example of how to inject a `DatabaseConfig` instance for the default database (i.e., the database named `default` in your configuration):

@[di-database-config](code/DI.scala)

Injecting a `DatabaseConfig` instance for a different database is also easy. Simply prepend the annotation `@NamedDatabase("<db-name>")` to the `dbConfigProvider` constructor parameter:

@[named-di-database-config](code/DI.scala)

Of course, you should replace the string `"<db-name>"` with the name of the database's configuration you want to use.

For a full example, have a look at [this sample projet](https://github.com/playframework/play-slick/tree/master/samples/di).

### DatabaseConfig via Global Lookup

Here is an example of how to lookup a `DatabaseConfig` instance for the default database (i.e., the database named `default` in your configuration):

@[global-lookup-database-config](code/GlobalLookup.scala)

Looking up a `DatabaseConfig` instance for a different database is also easy. Simply pass the database name:

@[named-global-lookup-database-config](code/GlobalLookup.scala)

Of course, you should replace the string `"<db-name>"` with the name of the database's configuration you want to use.

For a full example, have a look at [this sample projet](https://github.com/playframework/play-slick/tree/master/samples/basic).

### Running a database query in a Controller

To run a database query in your controller, you will need both a Slick database and driver. Fortunately, from the above we now know how to obtain a Slick `DatabaseConfig`, hence we have what we need to run a database query.

You will need to import some types and implicits from the driver:

@[driver-import](code/GlobalLookup.scala)

And then you can define a controller's method that will run a database query:

@[action-with-db](code/GlobalLookup.scala)

That's just like using stock Play and Slick!

## Configuring the connection pool

Read [[here|PlaySlickAdvancedTopics#Connection-Pool]] to find out how to configure the connection pool.