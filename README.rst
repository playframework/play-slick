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

DB wrapper
`````````````
The DB wrapper is just a thin wrapper that uses Slicks Database classes with databases in the Play Application . 

This is an example usage:

    import play.api.db.slick.Config.driver.simple._

    play.api.db.slick.DB.withSession{ implicit session =>
      Users.insert(User("fredrik","ekholdt"))
    }


Multiple datasources and drivers
`````````````
You can specify multiple drivers.

Default driver is set in the application.conf file with the ``db.default.driver`` property.
You can change `default` by another datasource name, for example``db.test.driver`` for tests.

Then you can use this configuration like this :

	 play.api.db.slick.DB("test").withSession { implicit session =>
	 	 Users.insert(User("fredrik","ekholdt"))
	 }


Copyright
---------

Copyright: Typesafe 2013
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
