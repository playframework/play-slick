[![Build Status](https://travis-ci.org/playframework/play-slick.png?branch=master)](https://travis-ci.org/playframework/play-slick)

# About

The Play Slick module makes [Slick] a first-class citizen of [Play].

The Play Slick module consists of two features:

  - Integration of Slick into Play's application lifecycle.
  - Support for Play database evolutions.

Because Slick creates and manages both a connection pool and a thread pool, integrating Slick with Play boils down to ensure that all resources allocated by Slick are shutted down when a Play application is stopped (or reloaded).

# Versioning

The Play Slick plugin supports several different versions of Play and Slick.

| Plugin version         | Play version       | Slick version       | Scala version |
|------------------------|--------------------|---------------------|---------------|
| 0.4.x                  | 2.1.x              | 1.0.x               | 2.10.x        |
| 0.5.x                  | 2.2.x              | 1.0.x               | 2.10.x        |
| 0.6.x                  | 2.2.x              | 2.0.x               | 2.10.x        |
| 0.7.x                  | 2.3.x              | 2.0.x               | 2.10.x        |
| 0.8.x                  | 2.3.x              | 2.1.x               | 2.10.x/2.11.x |
| 1.0.0                  | 2.4.x              | 3.0.x               | 2.10.x/2.11.x |

The plugin has its own release cycle and therefore is not integrated into either core Play or Slick.

[Play]: https://www.playframework.com
[Slick]: http://slick.typesafe.com

## Current Version

For Play 2.4 and Slick 3.0 (with Scala 2.10 or Scala 2.11):

```scala
"com.typesafe.play" %% "play-slick" % "1.0.0-RC1"
```

# Documentation

The documentation for the latest release is available [here](https://www.playframework.com/documentation/2.4.x/PlaySlick).

Documentation for v0.8 is available in the project's [wiki](https://github.com/playframework/play-slick/wiki).

# Copyright

Copyright: Typesafe 2013-2015
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html