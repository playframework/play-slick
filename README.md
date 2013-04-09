# About

This plugin makes Slick a first-class citizen of Play 2.1.

The play-slick plugins consists of 2 parts:
 - DDL schema generation Plugin that works like the Ebean DDL Plugin. Based on config it generates create schema and drop schema SQL commands and writes them to evolutions.
 - A wrapper DB object that uses the datasources defined in the Play config files. It is there so it is possible to use Slick sessions in the same fashion as you would Anorm JDBC connections.

The *intent* is to get this plugin into Play 2.2 if possible.



# Setup
In your application, add this configuration to the `project/Build.scala` file :

```scala
val appDependencies = Seq(
  //your dependencies
  "com.typesafe" %% "play-slick" % "0.3.2-SNAPSHOT" 
)

 val main = play.Project(appName, appVersion, appDependencies).settings(
  //your settings
    resolvers += "Sonatype SNAPSHOTS" at "https://oss.sonatype.org/content/repositories/snapshots") 
```

Note that only Play 2.1.1 is supported.

Please read more on the [wiki](https://github.com/freekh/play-slick/wiki/Usage)

Copyright
---------

Copyright: Typesafe 2013
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
