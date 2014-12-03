# Using Play Slick

The Play Slick module makes [Slick](http://slick.typesafe.com/) a first-class citizen of Play.

The Play Slick module consists of 3 features:

  - A wrapper DB object that uses the datasources defined in the Play config files, and pulls them from a connection pool. It is there so it is possible to use Slick sessions in the same fashion as you would Anorm JDBC connections. There are some smart caching and load balancing that makes your connections to your DB more performant.
  - A DDL plugin, that reads Slick tables and automatically creates schema updates on reload. This is useful in particular for demos and to get started.
  - A wrapper to use play enumeratees together with Slick.


## Setup

Add a library dependency on play-slick. See the [play-slick README] for the latest library dependency and version information.

Configure the database in your **application.conf** as for other Play databases. For example:

```conf
db.default.driver=org.h2.Driver
db.default.url="jdbc:h2:mem:play"
db.default.user=sa
db.default.password=""
```

Using the following import:

@[driver-import](code/ScalaSlick.scala)

will import the Slick driver based on the `db.default.driver` configuration in `application.conf`, or the one set by the test helpers in test mode.

If you need to use more than one database driver per mode (run or test), please read the information about [[using multiple drivers|ScalaSlickDrivers]].

For more information about creating models see the [Slick documentation].

[play-slick README]: https://github.com/playframework/play-slick
[Slick documentation]: http://slick.typesafe.com/docs


## DBAction

Use the DBAction in lieu of Action when you need an implicit Session in your controller.

The DBAction is predicated, meaning that it returns an error if the amount of requests are higher than a threshold. This means that your users will get an error when traffic is too high, instead of taking down the whole system.

Import DBAction and useful implicits with:

@[dbaction-import](code/ScalaSlick.scala)

Example usage of DBAction:

@[dbaction-session](code/ScalaSlick.scala)

> Note: `rs` contains the Play request and the Slick session. You can access the Play request via `rs.request` and the Slick session via `rs.dbSession`.

If you find yourself in need of combining the DBAction, consider using the DB wrapper directly instead.

It is also possible to extend the DefaultDBAction or PredicatedDBAction to customise behaviour. The source code contains more descriptions of how to do this: https://github.com/playframework/play-slick/blob/master/code/src/main/scala/play/api/db/slick/DBAction.scala#L16


## DDL plugin

In order to enable DDL schema generation you must specify the packages or classes you want to have in the `application.conf` file:

```conf
slick.default="models.*"
```

It follows the same format as the Ebean plugin: `models.*` means all Tables in the models package should be run on the default database.

It is possible to specify individual objects like:

```conf
slick.default="models.Users,models.Settings"
```


## DB wrapper

The `DB` wrapper is just a thin wrapper that uses Slick's `Database` classes with databases in the Play application.

You can import `DB` with:

@[db-import](code/ScalaSlick.scala)

A Play `Application` can be passed to `DB` explicitly, or to use the currently running Play application implicitly use the following import:

@[import-implicit-app](code/ScalaSlick.scala)

Here's an example of using the DB wrapper:

@[db-session](code/ScalaSlick.scala)

For transactions use the `withTransaction` method:

@[db-transaction](code/ScalaSlick.scala)
