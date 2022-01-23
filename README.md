# Play Slick

[![Build Status](https://travis-ci.org/playframework/play-slick.png?branch=master)](https://travis-ci.org/playframework/play-slick) [![codecov.io](https://codecov.io/github/playframework/play-slick/coverage.svg?branch=master)](https://codecov.io/github/playframework/play-slick?branch=master)

The Play Slick module makes [Slick] a first-class citizen of [Play]. It consists of two features:

- Integration of Slick into Play's application lifecycle.
- Support for Play database evolutions.

Because Slick creates and manages both a connection pool and a thread pool, integrating Slick with Play boils down to ensuring that all resources allocated by Slick are shutdown when a Play application is stopped (or reloaded).

[Play]: https://www.playframework.com
[Slick]: https://scala-slick.org/

The plugin has its own release cycle and therefore is not integrated into either core Play or Slick.

## Current Version

To use play-slick, you need to add the following dependencies:

```scala
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
)
```

Or use a version that is compatible with the Play version you are using. See all available versions below.

## All Releases

The Play Slick plugin supports several different versions of Play and Slick.

| Plugin version | Play version | Slick version | Scala version        |
|----------------|--------------|---------------|----------------------|
| 5.0.x          | 2.8.x        | 3.3.2+        | 2.12.x/2.13.x        |
| 4.0.2+         | 2.7.x        | 3.3.2+        | 2.11.x/2.12.x/2.13.x |
| 4.0.x          | 2.7.x        | 3.3.x         | 2.11.x/2.12.x        |
| 3.0.x          | 2.6.x        | 3.2.x         | 2.11.x/2.12.x        |
| 2.1.x          | 2.5.x        | 3.2.0         | 2.11.x               |
| 2.0.x          | 2.5.x        | 3.1.0         | 2.11.x               |
| 1.1.x          | 2.4.x        | 3.1.0         | 2.10.x/2.11.x        |
| 1.0.1          | 2.4.x        | 3.0.1         | 2.10.x/2.11.x        |
| 1.0.0          | 2.4.x        | 3.0.0         | 2.10.x/2.11.x        |
| 0.8.x          | 2.3.x        | 2.1.0         | 2.10.x/2.11.x        |
| 0.7.0          | 2.3.x        | 2.0.2         | 2.10.x               |
| 0.6.1          | 2.2.x        | 2.0.x         | 2.10.x               |
| 0.5.1          | 2.2.x        | 1.0.x         | 2.10.x               |

> * Release Candidate: these releases are not stable and should not be used in production.

Note that the `+` next to a version means that the specified version and later trailing point releases are supported by the same version of the play-slick plugin. While a `x` means that any trailing point release is supported by the same version of play-slick.

## Documentation

The documentation for the latest release is available [here](https://www.playframework.com/documentation/latest/PlaySlick).

## Copyright

Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>.

License: Apache License 2.0, <http://www.apache.org/licenses/LICENSE-2.0.html>
