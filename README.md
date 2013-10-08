# About

This plugin makes [Slick](http://slick.typesafe.com/) a first-class citizen of Play 2.2.

The play-slick plugins consists of 2 parts:
 - DDL schema generation Plugin that works like the Ebean DDL Plugin. Based on config it generates create schema and drop schema SQL commands and writes them to evolutions.
 - A wrapper DB object that uses the datasources defined in the Play config files. It is there so it is possible to use Slick sessions in the same fashion as you would Anorm JDBC connections.


# Setup

In the `project/Build.scala` file add::

In your application, add `"com.typesafe.play" %% "play-slick" % "0.5.0.5"` to the appDependencies in your `project/Build.scala` file:

to your `play.Project`.

Example :

```scala
 val appDependencies = Seq(
   //other deps
  "com.typesafe.play" %% "play-slick" % "0.5.0.5" 
 )
```

Add `slick.default="models.*"` and:
```
db.default.driver=org.h2.Driver
db.default.url="jdbc:h2:mem:play"
db.default.user=sa
db.default.password=""
```
to **application.conf** and [create a model](http://slick.typesafe.com/doc/1.0.1/lifted-embedding.html#tables).



Note that only Play 2.2.x is supported by the 0.5.x series.
The Play 2.1 was supported in the 0.4.x series.

Please read more about usage on the [wiki](https://github.com/freekh/play-slick/wiki)

Copyright
---------

Copyright: Typesafe 2013
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
