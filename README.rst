About
-----
This plugin makes Slick a first-class citizen of Play 2.1.

The play-slick plugins consists of 2 parts:
# DDL schema generation Plugin that works like the Ebean DDL Plugin. Based on config it generates create schema and drop schema SQL commands and writes them to evolutions.
# A wrapper DB object that uses the datasources defined in the Play config files. It is there so it is possible to use Slick sessions in the same fashion as you would Anorm JDBC connections.

The *intent* is to get this plugin into Play 2.2 if possible.

Usage
-----
Currently you must clone this project and publish it locally using sbt 0.12: ``sbt publish-local``

Then in the ``project/Build.scala`` file add: ``"com.typesafe" %% "play-slick" % "0.2.7-SNAPSHOT"`` to the list of ``appDependencies``

DDL Plugin
`````````````
In order to enable DDL schema generation you must specify the packages or classes you want to have in the ``application.conf`` file:
``slick.default="models.*"``
It follows the same format as the Ebean plugin: ``slick.default="models.*"`` means all Tables in the models package should be run on the default database.

It is possible to specify individual objects like: ``slick.default="models.Users,models.Settings"``

DB wrapper
`````````````
The DB wrapper is just a thin wrapper Slicks Database classes. 

This is an example usage:
  play.api.db.slick.DB.withSession{ implicit session =>
    Users.insert(User("fredrik","ekholdt"))
  }


Copyright
---------

Copyright: Typesafe 2013
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
