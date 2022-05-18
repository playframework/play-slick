# Play Slick

[![Twitter Follow](https://img.shields.io/twitter/follow/playframework?label=follow&style=flat&logo=twitter&color=brightgreen)](https://twitter.com/playframework)
[![Discord](https://img.shields.io/discord/931647755942776882?logo=discord&logoColor=white)](https://discord.gg/g5s2vtZ4Fa)
[![GitHub Discussions](https://img.shields.io/github/discussions/playframework/playframework?&logo=github&color=brightgreen)](https://github.com/playframework/playframework/discussions)
[![StackOverflow](https://img.shields.io/static/v1?label=stackoverflow&logo=stackoverflow&logoColor=fe7a16&color=brightgreen&message=playframework)](https://stackoverflow.com/tags/playframework)
[![YouTube](https://img.shields.io/youtube/channel/views/UCRp6QDm5SDjbIuisUpxV9cg?label=watch&logo=youtube&style=flat&color=brightgreen&logoColor=ff0000)](https://www.youtube.com/channel/UCRp6QDm5SDjbIuisUpxV9cg)
[![Twitch Status](https://img.shields.io/twitch/status/playframework?logo=twitch&logoColor=white&color=brightgreen&label=live%20stream)](https://www.twitch.tv/playframework)
[![OpenCollective](https://img.shields.io/opencollective/all/playframework?label=financial%20contributors&logo=open-collective)](https://opencollective.com/playframework)

[![Build Status](https://github.com/playframework/play-slick/actions/workflows/build-test.yml/badge.svg)](https://github.com/playframework/play-slick/actions/workflows/build-test.yml)
[![Maven](https://img.shields.io/maven-central/v/com.typesafe.play/play-slick_2.13.svg?logo=apache-maven)](https://mvnrepository.com/artifact/com.typesafe.play/play-slick_2.13)
[![Repository size](https://img.shields.io/github/repo-size/playframework/play-slick.svg?logo=git)](https://github.com/playframework/play-slick)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![Mergify Status](https://img.shields.io/endpoint.svg?url=https://api.mergify.com/v1/badges/playframework/play-slick&style=flat)](https://mergify.com)

The Play Slick module makes [Slick] a first-class citizen of [Play]. It consists of two features:

- Integration of Slick into Play's application lifecycle.
- Support for Play database evolutions.

Because Slick creates and manages both a connection pool and a thread pool, integrating Slick with Play boils down to ensuring that all resources allocated by Slick are shutdown when a Play application is stopped (or reloaded).

[Play]: https://www.playframework.com
[Slick]: https://scala-slick.org/

The plugin has its own release cycle and therefore is not integrated into either core Play or Slick.

Examples of `play-slick`s usage can be found [here](https://github.com/playframework/play-samples).

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

This library is [Community Driven](https://developer.lightbend.com/docs/introduction/getting-help/support-terminology.html)
