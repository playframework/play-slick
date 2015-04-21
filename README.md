# About

The Play Slick module makes [Slick](http://slick.typesafe.com/) a first-class citizen of Play.

The Play Slick module consists of 3 features:

  - Integration of Slick into Play's application lifecycle. Because Slick now creates and manages both a connection pool and a thread pool, integrating Slick with Play boils down to ensure that all resources allocated by Slick are shutted down when a Play application is stopped (or reloaded).
  - A DDL plugin, that reads Slick tables and automatically creates schema updates on reload. This is useful in particular for demos and to get started.
  - Support for Play database evolutions.

[![Build Status](https://travis-ci.org/playframework/play-slick.png?branch=master)](https://travis-ci.org/playframework/play-slick)

# Versioning

The Play Slick plugin supports several different versions of Play and Slick.

| Plugin version         | Play version       | Slick version       | Scala version |
|------------------------|--------------------|---------------------|---------------|
| 0.4.x                  | 2.1.x              | 1.0.x               | 2.10.x        |
| 0.5.x                  | 2.2.x              | 1.0.x               | 2.10.x        |
| 0.6.x                  | 2.2.x              | 2.0.x               | 2.10.x        |
| 0.7.x                  | 2.3.x              | 2.0.x               | 2.10.x        |
| 0.8.x                  | 2.3.x              | 2.1.x               | 2.10.x/2.11.x |
| 0.9.x (M4 and earlier) | 2.4.x              | 2.1.x               | 2.10.x/2.11.x |
| 0.9.x (M5 and later)   | 2.4.x              | 3.0.x               | 2.10.x/2.11.x |

The plugin has its own release cycle and therefore is not integrated into either core Play or Slick.

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
"com.typesafe.play" %% "play-slick" % "0.9.0-M3"
```


# Copyright

Copyright: Typesafe 2013-2014
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
