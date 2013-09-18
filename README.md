# About

This plugin makes [Slick](http://slick.typesafe.com/) a first-class citizen of Play 2.2.

The play-slick plugins consists of 2 parts: 
 - A wrapper DB object that uses the datasources defined in the Play config files, and pulls them from a connection pool. It is there so it is possible to use Slick sessions in the same fashion as you would Anorm JDBC connections.


# Setup

In the `project/Build.scala` file add::

```scala
.dependsOn(RootProject( uri("http://git.safespeedllc.com:9001/safespeed/play-slick.git") ))
```

to your `play.Project`.

Example :

```scala
val main = play.Project(appName, appVersion, appDependencies).settings(
  // Add your own project settings here      
 ).dependsOn(RootProject( uri("http://git.safespeedllc.com:9001/safespeed/play-slick.git") ))
```

Currently only slick 2.0 is supported with no planned support for evolutions. (Worthless gimmick anyway)

Please read more about usage on the [wiki](https://github.com/freekh/play-slick/wiki)

Copyright
---------

Copyright: Typesafe 2013
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html