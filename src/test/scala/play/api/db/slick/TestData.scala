package play.api.db.slick

import play.api.Configuration

object TestData {
  val configuration: Configuration = Configuration.from(
    Map(
      "slick.dbs.somedb.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.somedb.db.driver" -> "org.h2.Driver",

      "slick.dbs.default.driver" -> "slick.driver.MySQLDriver$",
      "slick.dbs.default.db.driver" -> "com.mysql.jdbc.Driver",

      "slick.dbs.jdbc-driver-not-recognized.driver" -> "slick.driver.MySQLDriver$",
      "slick.dbs.jdbc-driver-not-recognized.db.driver" -> "play.api.db.slick.SomeDummyDriver"
  ))

  val badConfiguration: Configuration = configuration ++ Configuration.from(
    Map("slick.dbs.missing-slick-driver.db.driver" -> "play.api.db.slick.SomeDummyDriver"))

}