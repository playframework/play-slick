# Using Play Slick

The Play Slick module makes [Slick](https://scala-slick.org/) a first-class citizen of Play, and consists of two primary features:

- Integration of Slick into Play's application lifecycle.
- Support for [[Play database evolutions|Evolutions]].

Play Slick currently supports Slick 3.3 with Play 2.8, for Scala 2.12 and Scala 2.13.

Previous versions support previous versions of Play! as well as Scala 2.11 and Scala 2.12.

> **Note**: This guide assumes you already know both Play 2.8 and Slick 3.3.

## Getting Help

If you are having trouble using Play Slick, check if the [[FAQ|PlaySlickFAQ]] contains the answer. Otherwise, feel free to reach out to [play-framework user community](https://discuss.playframework.com). Also, note that if you are seeking help on Slick, the [slick user community](https://scala-slick.org/community/) may be a better place.

Finally, if you prefer to get an answer for your Play and Slick questions in a timely manner, and with a well-defined SLA, you may prefer [to get in touch with Lightbend](https://www.lightbend.com/subscription), as it offers commercial support for these technologies.

## About this release

Users of previous versions of Play Slick will notice a number of major changes. Read the [[migration guide|PlaySlickMigrationGuide]] for details on upgrading from older versions of Play Slick.

First time users of Play Slick will appreciate the painless integration of Slick into Play. If you're familiar with Play and Slick, configuring and interacting with the Play Slick module will be straightforward.

## Setup

Add a library dependency on play-slick:

@[add-library-dependencies](code/slick.sbt)

The above dependency will also bring along the Slick library as a transitive dependency. This implies you don't need to add an explicit dependency on Slick, but you may if desired. You may explicitly define a dependency to Slick if you need to use a newer version than the one bundled with play-slick. Because Slick trailing dot releases are binary compatible, you won't incur any risk in using a different Slick trailing point release than the one that was used to build play-slick.

### Support for Play database evolutions

Play Slick supports [[Play database evolutions|Evolutions]].

To enable evolutions, you will need the following dependencies:

@[add-dependency-with-evolutions](code/slick.sbt)

Note there is no need to add the Play `evolutions` component to your dependencies, as it is a transitive dependency of the `play-slick-evolutions` module.

### JDBC driver dependency

The Play Slick module does not bundle any JDBC driver. Hence, you will need to explicitly add the JDBC driver(s) you want to use in your application. For instance, if you would like to use an in-memory database such as H2, you will have to add a dependency to it:

```scala
"com.h2database" % "h2" % "${H2_VERSION}" // replace `${H2_VERSION}` with an actual version number
```

## Database Configuration

To have the Play Slick module handling the lifecycle of Slick databases, it is important that you never create database instances explicitly in your code. Rather, you should provide a valid Slick driver and database configuration in your **application.conf** (by convention the default Slick database must be called `default`):

```conf
# Default database configuration
slick.dbs.default.profile="slick.jdbc.H2Profile$"
slick.dbs.default.db.driver="org.h2.Driver"
slick.dbs.default.db.url="jdbc:h2:mem:play"
```

First, note that the above is a valid Slick configuration (for the complete list of configuration parameters that you can use to configure a database see the Slick ScalaDoc for [Database.forConfig](https://scala-slick.org/doc/3.3.2/api/index.html#slick.jdbc.JdbcBackend$DatabaseFactoryDef@forConfig%28path:String,config:com.typesafe.config.Config,driver:java.sql.Driver,classLoader:ClassLoader%29:JdbcBackend.this.Database) - make sure to expand the `forConfig` row in the doc).

Second, the `slick.dbs` prefix before the database's name is configurable. In fact, you may change it by overriding the value of the configuration key `play.slick.db.config`.

Third, in the above configuration `slick.dbs.default.profile` is used to configure the Slick profile, while `slick.dbs.default.db.driver` is the underlying JDBC driver used by Slick's backend. In the above configuration we are configuring Slick to use H2 database, but Slick supports several other databases. Check the [Slick documentation](https://scala-slick.org/docs/) for a complete list of supported databases, and to find a matching Slick driver.

Slick does not support the `DATABASE_URL` environment variable in the same way as the default Play JBDC connection pool. But starting in version 3.0.3, Slick provides a `DatabaseUrlDataSource` specifically for parsing the environment variable.

```conf
slick.dbs.default.profile="slick.jdbc.PostgresProfile$"
slick.dbs.default.db.dataSourceClass = "slick.jdbc.DatabaseUrlDataSource"
slick.dbs.default.db.properties.driver = "org.postgresql.Driver"
```

On some platforms, such as Heroku, you may [substitute](https://github.com/lightbend/config/blob/master/HOCON.md#substitution-fallback-to-environment-variables) the `JDBC_DATABASE_URL` environment variable, which is in the format `jdbc:vendor://host:port/db?args`, if it is available. For example:

```conf
slick.dbs.default.profile="slick.jdbc.PostgresProfile$"
slick.dbs.default.db.driver="org.postgresql.Driver"
slick.dbs.default.db.url=${JDBC_DATABASE_URL}
```

> **Note**: Failing to provide a valid value for both `slick.dbs.default.profile` and `slick.dbs.default.db.driver` will lead to an exception when trying to run your Play application.

To configure several databases:

```conf
# Orders database
slick.dbs.orders.profile="slick.jdbc.H2Profile$"
slick.dbs.orders.db.driver="org.h2.Driver"
slick.dbs.orders.db.url="jdbc:h2:mem:play"

# Customers database
slick.dbs.customers.profile="slick.jdbc.H2Profile$"
slick.dbs.customers.db.driver="org.h2.Driver"
slick.dbs.customers.db.url="jdbc:h2:mem:play"
```

If something isn't properly configured, you will be notified in your browser:

[[images/database-config-error.png]]

> Note: Your application will be started only if you provide a valid Slick configuration.

## Usage

After having properly configured a Slick database, you can obtain a `DatabaseConfig` (which is a Slick type bundling a database and driver) by using dependency injection.

> Note: A Slick database instance manages a thread pool and a connection pool. In general, you should not need to shut down a database explicitly in your code (by calling its `close` method), as the Play Slick module takes care of this already.

### DatabaseConfig via runtime dependency injection

While you can get the `DatabaseConfig` instance manually by accessing the `SlickApi`, we've provided some helpers for runtime DI users (Guice, Scaldi, Spring, etc.) for obtaining specific instances within your controller.

Here is an example of how to inject a `DatabaseConfig` instance for the default database (i.e., the database named `default` in your configuration):

@[di-database-config](code/DI.scala)

In this example we're also injecting Play's default `ExecutionContext`, which will be used implicitly in the future transformations below.

Injecting a `DatabaseConfig` instance for a different database is also easy. You can simply prepend the annotation `@NamedDatabase("<db-name>")` to the `dbConfigProvider` constructor parameter:

@[named-di-database-config](code/DI.scala)

Of course, you should replace the string `"<db-name>"` with the name of the database's configuration you want to use.

> Note: To access the database object, you need only call the function `db` on the `HasDatabaseConfig` trait. You do not need to reference the dbConfigProvider constructor parameter.

For a full example, have a look at [this sample project](https://github.com/playframework/play-slick/tree/master/samples/basic).

### Compile-time dependency injection

If you're using compile-time DI, you can query the database config directly from the `SlickApi` using the `slickApi.dbConfig(DbName(name))` method. The `play.api.db.slick.SlickComponents` provide access to the `slickApi`.

### Running a database query in a Controller

To run a database query in your controller, you will need both a Slick database and driver. Fortunately, from the above we now know how to obtain a Slick `DatabaseConfig`, hence we have what we need to run a database query.

You will need to import some types and implicits from the driver:

@[driver-import](code/Example.scala)

And then you can define a controller's method that will run a database query:

@[action-with-db](code/Example.scala)

That's just like using stock Play and Slick!

## Configuring the connection pool

Read [[here|PlaySlickAdvancedTopics#Connection-Pool]] to find out how to configure the connection pool.
