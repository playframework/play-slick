package play.api.db.slick

import play.api.Configuration

object TestData {
  private lazy val h2DatasourceConfig: Configuration = Configuration.from(
    Map(
      "slick.dbs.h2datasource.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.h2datasource.db.dataSourceClass" -> "slick.jdbc.DatabaseUrlDataSource",
      "slick.dbs.h2datasource.db.properties.driver" -> "org.h2.Driver",
      "slick.dbs.h2datasource.db.properties.url" -> "jdbc:h2:mem:"
    )
  )

  val configuration: Configuration = Configuration.from(
    Map(
      "slick.dbs.somedb.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.somedb.db.driver" -> "org.h2.Driver",
      "slick.dbs.somedb.db.url" -> "jdbc:h2:mem:",

      "slick.dbs.default.driver" -> "slick.driver.MySQLDriver$",
      "slick.dbs.default.db.driver" -> "com.mysql.jdbc.Driver",

      "slick.dbs.jdbc-driver-not-recognized.driver" -> "slick.driver.MySQLDriver$",
      "slick.dbs.jdbc-driver-not-recognized.db.driver" -> "play.api.db.slick.SomeDummyDriver"
  )) ++ h2DatasourceConfig

  val badConfiguration: Configuration = configuration ++ Configuration.from(
    Map("slick.dbs.missing-slick-driver.db.driver" -> "play.api.db.slick.SomeDummyDriver"))

}