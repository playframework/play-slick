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
    import slick.driver.H2Driver.simple._

    play.api.db.slick.DB.withSession{ implicit session =>
      Users.insert(User("fredrik","ekholdt"))
    }


Or transactionally:
    import slick.driver.H2Driver.simple._

    play.api.db.slick.DB.withTransaction{ implicit session =>
      val list = Query(Users).filter(name === "fredrik")
      val updatedUsers = update(list)
      Users.insertAll(updatedUsers)
    }

Multiple datasources and drivers
`````````````
When having multiple datasources and drivers it is recommended to use the cake pattern.
Do not worry about the scary name it is quite easy.

Hava look in the `samples` for an example of this or keep reading.

For each table you have, create a self-type of `play.api.db.slick.Profile` and import everything from the `profile` on your table:
    trait UserComponent extends Profile { this: Profile =>
       import profile.simple._

       object Users extends Table[User]("USERS") { ... }
    }

Then you just have to put all your tables together into a DAO (data access object) like this:
    class DAO(override val profile: ExtendedProfile) extends UserComponent with FooComponent with BarComponent with Profile

Whenever you need to use you database you just new up your DAO and import everything in it and in its profile:
    package models

    import play.api.slick.DB
    object current {
      val DB = DB("mydb")
      val dao = new DAO(db.profile("mydb")) //or just DB.profile if you want "default"
    } 

In your `application.conf` you can now do:
    slick.default="models.current.*"

You can then use it like this:
    import current._
    import current.simple._
    
    DB.withSession{ implicit session => 
       Query(Users).list
    }

Pweeh, there are a certain amount of lines of code there, but works great and scales along with the life cycle of your app: from the start, when you need tests, when you change the DB, ... 


Copyright
---------

Copyright: Typesafe 2013
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
