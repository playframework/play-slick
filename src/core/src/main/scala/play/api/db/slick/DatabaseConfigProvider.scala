package play.api.db.slick

import slick.basic.BasicProfile
import slick.basic.DatabaseConfig

/**
 * Generic interface for a provider of a `DatabaseConfig` instance. A `DatabaseConfig` is Slick type
 * that bundles a database and profile.
 *
 * Usually, you shouldn't need to create instances of `DatabaseConfigProvider` explicitly. Rather, you
 * should rely on dependency injection. If you don't want to use dependency injection, then use the
 * companion object and call `DatabaseConfigProvider.get`.
 *
 * ==Example==
 *
 * Here is an example of how you can use dependency injection to obtain an instance of `DatabaseConfigProvider`,
 * for the database named `default` in your **application.conf**.
 * {{{
 * class Application @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
 *  // ...
 * }
 * }}}
 *
 * While here is an example for injecting a `DatabaseConfigProvider` for a database named `orders` in your
 * **application.conf**.
 * {{{
 * import play.db.NamedDatabase
 * class Application @Inject()(@NamedDatabase("orders") protected val dbConfigProvider: DatabaseConfigProvider) {
 *  // ...
 * }
 * }}}
 */
trait DatabaseConfigProvider {
  def get[P <: BasicProfile]: DatabaseConfig[P]
}

/**
 * Look up a `DatabaseConfig` (which is Slick type that bundles a database and profile) for the passed
 * database name. The `DatabaseConfig` instance is created using the database's configuration you have
 * provided in your **application.conf**, for the passed database name.
 *
 * Note that if no database name is passed, `default` is used, and hence the configuration
 * `slick.dbs.default` is used to create the `DatabaseConfig` instance.
 *
 * ==Example==
 *
 * Here is an example for obtaining a `DatabaseConfig` instance for the database named `default` in
 * your **application.conf**.
 * {{{
 * import play.api.Play
 * import play.api.db.slick.DatabaseConfigProvider
 * import slick.profile.RelationalProfile
 * val dbConfig = DatabaseConfigProvider.get[RelationalProfile](Play.current)
 * }}}
 *
 * While here is an example for obtaining a `DatabaseConfig` instance for the database named `orders`
 * in your **application.conf**.
 * {{{
 * import play.api.Play
 * import play.api.db.slick.DatabaseConfigProvider
 * import slick.profile.RelationalProfile
 * val dbConfig = DatabaseConfigProvider.get[RelationalProfile]("orders")(Play.current)
 * }}}
 */
object DatabaseConfigProvider {
  import play.api.Application
  private object DatabaseConfigLocator {
    import play.api.Configuration
    private val slickApiCache = Application.instanceCache[SlickApi]
    private def slickApi(implicit app: Application): SlickApi = slickApiCache(app)

    private val configurationCache = Application.instanceCache[Configuration]
    private def configuration(implicit app: Application): Configuration = configurationCache(app)

    @throws(classOf[IllegalArgumentException])
    def apply[P <: BasicProfile](implicit app: Application): DatabaseConfig[P] = {
      val defaultDbName = configuration.underlying.getString(SlickModule.DefaultDbName)
      this(defaultDbName)
    }

    @throws(classOf[IllegalArgumentException])
    def apply[P <: BasicProfile](dbName: String)(implicit app: Application): DatabaseConfig[P] =
      slickApi.dbConfig[P](DbName(dbName))
  }

  /**
   * Returns a Slick database config for the `default` database declared in your **application.conf**.
   * Throws a IllegalArgumentException if your **application.conf** does not contain a configuration for
   * the `default` database.
   *
   * @return a Slick `DatabaseConfig` instance for the `default` database.
   */
  @throws(classOf[IllegalArgumentException])
  @deprecated(
    """Use DatabaseConfigProvider#get[P] or SlickApi#dbConfig[P]("default") on injected instances""".stripMargin,
    "3.0.0")
  def get[P <: BasicProfile](implicit app: Application): DatabaseConfig[P] =
    DatabaseConfigLocator(app)

  /**
   * Returns a Slick database config for the passed `dbName`.
   * Throws a IllegalArgumentException if no database configuration exist in your **application.conf**
   * for the passed `dbName`.
   *
   * @param dbName the name of a database in your **application.conf**.
   * @return a Slick `DatabaseConfig` instance for the requested database name.
   */
  @throws(classOf[IllegalArgumentException])
  @deprecated(
    """Inject DatabaseConfigProvider using @Named("dbName") and call get[P] or use SlickApi#dbConfig[P](name)""",
    "3.0.0")
  def get[P <: BasicProfile](dbName: String)(implicit app: Application): DatabaseConfig[P] =
    DatabaseConfigLocator(dbName)
}

/**
 * Mix-in this trait if you need a Slick database and profile. This is useful if you need to define a Slick
 * table or need to execute some operation in the database.
 *
 * There is only one abstract field, `dbConfig`, which you can implement by calling `DatabaseConfigProvider.get`.
 * If you are injecting `DatabaseConfigProvider` instances using dependency injection, prefer using the trait
 * `HasDatabaseConfigProvider` instead of this one.
 *
 * ==Example==
 *
 * {{{
 * // model definition
 * class Cat(name: String, color: String)
 * // DAO definition
 * class CatDAO extends HasDatabaseConfig[RelationalProfile] {
 * protected val dbConfig = DatabaseConfigProvider.get[RelationalProfile](Play.current)
 * import profile.api._
 *
 * private val Cats = TableQuery[CatsTable]
 * def all() = db.run(Cats.result)
 * def insert(cat: Cat) = db.run(Cats += cat)
 *
 * // Slick table definition
 * private class CatsTable(tag: Tag) extends Table[Cat](tag, "CAT") {
 * def name = column[String]("NAME", O.PrimaryKey)
 * def color = column[String]("COLOR")
 * def * = (name, color) <> (Cat.tupled, Cat.unapply _)
 * }
 * }
 * }}}
 *
 * Of course, you do not need to define a DAO to use this trait (the above it is really just an example of usage).
 */
trait HasDatabaseConfig[P <: BasicProfile] {
  /** The Slick database configuration. */
  protected val dbConfig: DatabaseConfig[P] // field is declared as a val because we want a stable identifier.
  /** The Slick profile extracted from `dbConfig`. */
  protected final lazy val profile: P = dbConfig.profile // field is lazy to avoid early initializer problems.
  @deprecated("Use `profile` instead of `driver`", "2.1")
  protected final lazy val driver: P = dbConfig.profile // field is lazy to avoid early initializer problems.
  /** The Slick database extracted from `dbConfig`. */
  protected final def db: P#Backend#Database = dbConfig.db
}

/**
 * Mix-in this trait if you need a Slick database and profile, and you are using dependency injection for obtaining
 * an instance of `DatabaseConfigProvider`. If you are not using dependency injection, then prefer mixing
 * `HasDatabaseConfig` instead.
 *
 * This trait is useful if you need to define a Slick table or need to execute some operation in the database.
 *
 * ==Example==
 *
 * {{{
 * // model definition
 * class Cat(name: String, color: String)
 * // DAO definition
 * class CatDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[RelationalProfile] {
 * import profile.api._
 *
 * private val Cats = TableQuery[CatsTable]
 * def all() = db.run(Cats.result)
 * def insert(cat: Cat) = db.run(Cats += cat)
 *
 * // Slick table definition
 * private class CatsTable(tag: Tag) extends Table[Cat](tag, "CAT") {
 * def name = column[String]("NAME", O.PrimaryKey)
 * def color = column[String]("COLOR")
 * def * = (name, color) <> (Cat.tupled, Cat.unapply _)
 * }
 * }
 * }}}
 *
 * Of course, you do not need to define a DAO to use this trait (the above it is really just an example of usage).
 */
trait HasDatabaseConfigProvider[P <: BasicProfile] extends HasDatabaseConfig[P] {
  /** The provider of a Slick `DatabaseConfig` instance.*/
  protected val dbConfigProvider: DatabaseConfigProvider
  override final lazy protected val dbConfig: DatabaseConfig[P] = dbConfigProvider.get[P] // field is lazy to avoid early initializer problems.
}
