##DDL plugin

In order to enable DDL schema generation you must specify the packages or classes you want to have in the `application.conf` file:
`slick.default="models.*"`
It follows the same format as the Ebean plugin: `slick.default="models.*"` means all Tables in the models package should be run on the default database.

It is possible to specify individual objects like: `slick.default="models.Users,models.Settings"`

##DB wrapper

The DB wrapper is just a thin wrapper that uses Slicks Database classes with databases in the Play Application . 

This is an example usage:
  
@[import](code/ScalaSlickUsage.scala)
@[insert](code/ScalaSlickUsage.scala)

Using `import play.api.db.slick.Config.driver.simple._` will import the driver defing with the key `db.default.driver` in application.conf, or the one set by the test helpers in test mode (see test section for more information).

You can use `DB.withTransaction` instead of `DB.withSession` to use a transaction.

Here is a configuration example for the default database : 

```conf
db.default.driver=org.h2.Driver
db.default.url="jdbc:h2:mem:play"
db.default.user=sa
db.default.password=""
```

If you need to use more than one database driver per mode (run or test), please read the [Using multiple drivers](https://github.com/playframework/play-slick/wiki/Using-multiple-drivers)!


