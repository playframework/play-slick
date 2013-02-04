About
-----
This plugin makes Slick a first-class citizen of Play 2.1.


The play-slick plugins consists of 2 parts:
 - DDL schema generation Plugin that works like the Ebean DDL Plugin. Based on config it generates create schema and drop schema SQL commands and writes them to evolutions.
 - A wrapper DB object that uses the datasources defined in the Play config files. It is there so it is possible to use Slick sessions in the same fashion as you would Anorm JDBC connections.

The *intent* is to get this plugin into Play 2.2 if possible.

Usage
-----
In the ``project/Build.scala`` file add::

    .dependsOn(RootProject( uri("git://github.com/freekh/play-slick.git") ))

to your ``play.Project``

Example::

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
     ).dependsOn(RootProject( uri("git://github.com/freekh/play-slick.git") ))
  

DDL Plugin
`````````````
In order to enable DDL schema generation you must specify the packages or classes you want to have in the ``application.conf`` file:
``slick.default="models.*"``
It follows the same format as the Ebean plugin: ``slick.default="models.*"`` means all Tables in the models package should be run on the default database.

It is possible to specify individual objects like: ``slick.default="models.Users,models.Settings"``

DAOs with mulitple drivers
`````````````
It is also possible to specify and search for inner object. This is useful if you need multiple drivers. 

Imagine a DAO is defined like this::

  class DAO(val driver: ExtendedProfile) {
      // Import the query language features from the driver
      import driver.simple._
  
      object Props extends Table[(String, String)]("properties") {
        def key = column[String]("key", O.PrimaryKey)
        def value = column[String]("value")
        def * = key ~ value
      }
    }


For the production code you could then have an instance of the DAO you would pass to the methods using said DAO::

    package db
    object default {
      implicit val dao = new DAO(H2Driver)
    }


And one for test::

    package db
    object test {
      implicit val dao = new DAO(SQLiteDriver)
    }


To be able to use DDL creation on this you simply use the complete path of the DAO object. You can then either use a wildcard or specify the object by using it is complete path.

Example using a wildcard: ``slick.default="db.default.*"``

Example specifying the exact Table: ``slick.default="db.default.dao.Props"`` 

DB wrapper
`````````````
The DB wrapper is just a thin wrapper that uses Slicks Database classes with databases in the Play Application . 

This is an example usage::

    play.api.db.slick.DB.withSession{ implicit session =>
      Users.insert(User("fredrik","ekholdt"))
    }


Issues
``````
Currently there is a bug that might make a test fail (usually after running mulitple tests)::

    [error] c.j.b.h.AbstractConnectionHook - Failed to acquire connection Sleeping for 1000ms and trying again. Attempts left: 1. Exception: null
    [error] c.j.b.ConnectionHandle - Database access problem. Killing off all remaining connections in the connection pool. SQL State = 08001

Copyright
---------

Copyright: Typesafe 2013
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
