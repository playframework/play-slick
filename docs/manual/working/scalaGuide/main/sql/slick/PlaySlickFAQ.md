# Play Slick FAQ

## What version should I use?

Have a look at the [compatibility matrix](https://github.com/playframework/play-slick#releases) to know what version you should be using.

## `play.db.pool` is ignored

It's indeed the case. Changing the value of `play.db.pool` won't affect what connection pool Slick is going to use. The reason is simply that Play Slick module currently doesn't support using a different connection pool than [HikariCP].

## Changing the connection pool used by Slick

While Slick allows using a different connection pool than [HikariCP](though, Slick currently only offers built-in support for HikariCP, and requires you to provide an implementation of [JdbcDataSourceFactory] if you want to use a different connection pool), Play Slick currently doesn't allow using a different connection pool than HikariCP. If you find yourself needing this feature, you can try to drop us a note on [playframework-dev].

## A binding to `play.api.db.DBApi` was already configured

If you get the following exception when starting your Play application:

```
1) A binding to play.api.db.DBApi was already configured at play.api.db.slick.evolutions.EvolutionsModule.bindings:
Binding(interface play.api.db.DBApi to ConstructionTarget(class play.api.db.slick.evolutions.internal.DBApiAdapter) in interface javax.inject.Singleton).
 at play.api.db.DBModule.bindings(DBModule.scala:25):
Binding(interface play.api.db.DBApi to ProviderConstructionTarget(class play.api.db.DBApiProvider))
```

It is very likely that you have [[enabled the jdbc plugin|ScalaDatabase]], and that doesn't really make sense if you are using Slick for accessing your databases. To fix the issue simply remove the Play *jdbc* component from your project's build.

Another possibility is that there is another Play module that is binding [DBApi](api/scala/play/api/db/DBApi.html) to some other concrete implementation. This means that you are still trying to use Play Slick together with another Play module for database access, which is likely not what you want.

## Play throws `java.lang.ClassNotFoundException: org.h2.tools.Server`

If you get the following exception when starting your Play application:

```java
java.lang.ClassNotFoundException: org.h2.tools.Server
        at java.net.URLClassLoader$1.run(URLClassLoader.java:372)
        at java.net.URLClassLoader$1.run(URLClassLoader.java:361)
        ...
```

It means you are trying to use a H2 database, but have forgot to add a dependency to it in your project's build. Fixing the problem is simple, just add the missing dependency in your project's build, e.g.,

```
"com.h2database" % "h2" % "${H2_VERSION}" // replace `${H2_VERSION}` with an actual version number
```

[playframework-dev]: https://groups.google.com/forum/#!forum/play-framework-dev 
[HikariCP]: http://brettwooldridge.github.io/HikariCP/
[JdbcDataSourceFactory]: http://slick.typesafe.com/doc/3.1.0/api/index.html#slick.jdbc.JdbcDataSourceFactory
