#About

This plugin makes Slick a first-class citizen of Play 2.1.

The play-slick plugins consists of 2 parts:
 - DDL schema generation Plugin that works like the Ebean DDL Plugin. Based on config it generates create schema and drop schema SQL commands and writes them to evolutions.
 - A wrapper DB object that uses the datasources defined in the Play config files. It is there so it is possible to use Slick sessions in the same fashion as you would Anorm JDBC connections.

The *intent* is to get this plugin into Play 2.2 if possible.

For [installation](https://github.com/freekh/play-slick/wiki/Installation) and [usage](https://github.com/freekh/play-slick/wiki/Usage), see the [wiki](https://github.com/freekh/play-slick/wiki)


Copyright
---------

Copyright: Typesafe 2013
License: Apache License 2.0, http://www.apache.org/licenses/LICENSE-2.0.html
