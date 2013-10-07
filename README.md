# About

This plugin makes [Slick](http://slick.typesafe.com/) a first-class citizen of Play 2.2.

The play-slick plugins consists of 2 parts: 
 - A wrapper DB object that uses the datasources defined in the Play config files, and pulls them from a connection pool. It is there so it is possible to use Slick sessions in the same fashion as you would Anorm JDBC connections.


# Setup

In the `project/Build.scala` file add::

In your application, add `"com.typesafe.play" %% "play-slick" % "0.5.0.4"` to the appDependencies in your `project/Build.scala` file:

to your `play.Project`.

Example :

```scala
 val appDependencies = Seq(
   //other deps
  "com.typesafe.play" %% "play-slick" % "0.5.0.4" 
 )
```

Currently only slick 2.0 is supported with no planned support for evolutions.

Please read more about usage on the [wiki](https://github.com/freekh/play-slick/wiki)

Copyright
---------

Copyright: Typesafe 2013
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
