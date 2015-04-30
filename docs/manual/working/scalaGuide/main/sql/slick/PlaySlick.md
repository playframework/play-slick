# Using Play Slick

The Play Slick module makes [Slick](http://slick.typesafe.com/) a first-class citizen of Play.

The Play Slick module consists of two features:

  - Integration of Slick into Play's application lifecycle.
  - Support for [[Play database evolutions|Evolutions]].

Play Slick currently supports Slick 3.0 with Play 2.4, for both Scala 2.10 and 2.11. If you need to use an older version of Play or Slick, have a look at the compatibility matrix in the [play-slick README] to know what version you should be using. Mind that the remainder of this guide explains how to use the latest release of Play Slick, and will not be appropriate if you are using version 0.8 or lower.

> Note: This guide assumes you already know both Play 2.4 and Slick 3.0. Also, it assumes you will be using the new Slick Database I/O Actions API. In fact, using the deprecated Invoker/Execution API of Slick should be possible, but it might not be as convenient. Also, be aware that the Invoker/Execution API is planned to be removed in the next major release of Slick.

## About this release

If you have been using a previous version of Play Slick, you will immediately notice that there have been quite a few major changes. It's recommended to read the [[migration guide|PlaySlickMigrationGuide]] for a smooth upgrade.

While, if this is the first time you are using Play Slick, you will appreciate that the integration of Slick in Play is quite austere. Meaning that if you know both Play and Slick, using Play Slick in your project should be straightforward.

## Setup

Add a library dependency on play-slick:

```scala
"com.typesafe.play" %% "play-slick" % "1.0.0-RC1"
```

The above dependency will also bring along the Slick library as a transitive dependency. This implies you don't need to add an explicit dependency on Slick.

Please, double-check in the [play-slick README] that the Play Slick version used here is indeed the latest release of Play Slick (you know it all too well, duplicated information has a tendency to become obsolete).

## Database Configuration

To have Play Slick module handling the lifecycle of Slick databases, it is important that you never create database's instances explicitly in your code. Rather, you should provide a valid Slick driver and database configuration in your **application.conf** (by convention the default Slick database must be called `default`):

```conf
# Default database configuration
slick.dbs.default.driver="slick.driver.H2Driver$"
slick.dbs.default.db.driver="org.h2.Driver"
slick.dbs.default.db.url="jdbc:h2:mem:play"
```

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

> Note: Play Slick module will only work if you provide a valid Slick configuration.

The examples above are all using H2, but Slick supports several other databases. Have a look at the [Slick documentation] for a complete list of supported databases (and to find the matching Slick driver to use).

Also, for the complete list of configuration parameters that you can use in your project's **application.conf**, see the Slick ScalaDoc for [Database.forConfig].

[play-slick README]: https://github.com/playframework/play-slick#versioning
[Slick documentation]: http://slick.typesafe.com/docs
[Database.forConfig]: http://slick.typesafe.com/doc/3.0.0-RC3/api/index.html#slick.jdbc.JdbcBackend$DatabaseFactoryDef@forConfig(String,Config,Driver):Database

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

## Support for Play Evolutions

Play Slick supports [[Play database evolutions|Evolutions]].