package play.api.db.slick

import play.api.Configuration

object TestData {
  private lazy val h2DatasourceConfig: Configuration = Configuration.from(
    Map(
      "slick.dbs.h2datasource.profile"              -> "slick.jdbc.H2Profile$",
      "slick.dbs.h2datasource.db.dataSourceClass"   -> "slick.jdbc.DatabaseUrlDataSource",
      "slick.dbs.h2datasource.db.properties.driver" -> "org.h2.Driver",
      "slick.dbs.h2datasource.db.properties.url"    -> "jdbc:h2:mem:"
    )
  )

  val configuration: Configuration = h2DatasourceConfig.withFallback(
    Configuration.from(
      Map(
        "slick.dbs.somedb.profile"                       -> "slick.jdbc.H2Profile$",
        "slick.dbs.somedb.db.driver"                     -> "org.h2.Driver",
        "slick.dbs.somedb.db.url"                        -> "jdbc:h2:mem:",
        "slick.dbs.default.profile"                      -> "slick.jdbc.MySQLProfile$",
        "slick.dbs.default.db.driver"                    -> "com.mysql.jdbc.Driver",
        "slick.dbs.jdbc-driver-not-recognized.profile"   -> "slick.jdbc.MySQLProfile$",
        "slick.dbs.jdbc-driver-not-recognized.db.driver" -> "play.api.db.slick.SomeDummyDriver"
      )
    )
  )

  val badConfiguration: Configuration = Configuration
    .from(Map("slick.dbs.missing-slick-profile.db.driver" -> "play.api.db.slick.SomeDummyDriver"))
    .withFallback(configuration)

}
