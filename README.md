# About

This plugin makes [Slick](http://slick.typesafe.com/) a first-class citizen of Play 2.x.

The play-slick plugins consists of 3 features:
 - A wrapper DB object that uses the datasources defined in the Play config files, and pulls them from a connection pool. It is there so it is possible to use Slick sessions in the same fashion as you would Anorm JDBC connections. There are some smart caching and load balancing that makes your connections to your DB more performant.
 - A DDL plugin, that reads Slick tables and automatically creates schema updates on reload. This is useful in particular for demos and to get started.
 - In addition it contains a wrapper to use play enumeratees together with Slick (thanks to Marc)

[![Build Status](https://travis-ci.org/playframework/play-slick.png?branch=master)](https://travis-ci.org/playframework/play-slick)

# Setup

Add a library dependency on play-slick:

```scala
"com.typesafe.play" %% "play-slick" % "{version}"
```

See below for the version matrix.

Configure the database in your **application.conf**. For example:

```
db.default.driver=org.h2.Driver
db.default.url="jdbc:h2:mem:play"
db.default.user=sa
db.default.password=""
```

For more information about creating models see the [Slick documentation].

[Slick documentation]: http://slick.typesafe.com/docs

# Usage
Please read more about usage on the [wiki](https://github.com/playframework/play-slick/wiki/Usage)

# Versioning

The Play Slick plugin supports several different versions of Play and Slick.

| Plugin version      | Play version       | Slick version       | Scala version |
|---------------------|--------------------|---------------------|---------------|
| 0.4.x               | 2.1.x              | 1.0.x               | 2.10.x        |
| 0.5.x               | 2.2.x              | 1.0.x               | 2.10.x        |
| 0.6.x               | 2.2.x              | 2.0.x               | 2.10.x        |
| 0.7.x               | 2.3.x              | 2.0.x               | 2.10.x        |
| 0.8.x               | 2.3.x              | 2.1.x               | 2.10.x/2.11.x |
| 0.9.x (milestone)   | 2.4.x              | 2.1.x               | 2.10.x/2.11.x |

The plugin has its own release cycle and therefore is not integrated into either core Play or Slick.

## Current Versions

For Play 2.2 and Slick 2.0 (with Scala 2.10):

```scala
"com.typesafe.play" %% "play-slick" % "0.6.1"
```

For Play 2.3 and Slick 2.0 (with Scala 2.10):

```scala
"com.typesafe.play" %% "play-slick" % "0.7.0"
```

For Play 2.3 and Slick 2.1 (with Scala 2.10 or 2.11):

```scala
"com.typesafe.play" %% "play-slick" % "0.8.1"
```

For Play 2.4 **milestone** and Slick 2.1 (with Scala 2.10 or 2.11):

```scala
"com.typesafe.play" %% "play-slick" % "0.9.0-M1"
```


# Copyright

Copyright: Typesafe 2013-2014
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
