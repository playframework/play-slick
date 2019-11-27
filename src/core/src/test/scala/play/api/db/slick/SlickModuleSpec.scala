package play.api.db.slick

import org.specs2.matcher.ValueCheck.typedValueCheck
import org.specs2.mutable.Specification

import play.api.db.slick.util.WithReferenceConfig
import play.api.inject.BindingKey
import play.api.inject.QualifierInstance
import play.api.inject.guice.GuiceApplicationBuilder
import play.db.NamedDatabaseImpl

class SlickModuleSpec extends Specification {
  "reference.conf" should {
    "slick module is enabled" in new WithReferenceConfig {
      enabledModules(ref) must contain(classOf[SlickModule].getName)
    }
    "provide a database config default path" in new WithReferenceConfig {
      val dbsKey = ref.getOptional[String](SlickModule.DbKeyConfig)
      dbsKey must beSome("slick.dbs")
    }

    "provide a name for the default database" in new WithReferenceConfig {
      val dbsKey = ref.getOptional[String](SlickModule.DefaultDbName)
      dbsKey must beSome("default")
    }
  }

  "SlickModule" should {
    val appBuilder = GuiceApplicationBuilder(configuration = TestData.configuration)
    val injector   = appBuilder.injector()

    "bind SlickApi to DefaultSlickApi" in {
      val api = injector.instanceOf[SlickApi]
      api must beAnInstanceOf[DefaultSlickApi]
    }
    "bind SlickApi as a singleton" in {
      val api1 = injector.instanceOf[SlickApi]
      val api2 = injector.instanceOf[SlickApi]
      api1 mustEqual api2
    }
    "bind the default database to a DatabaseConfigProvider" in {
      val dbConfProvider = injector.instanceOf[DatabaseConfigProvider]
      dbConfProvider must not(beNull)
    }
    "return a DatabaseConfigProvider with a DatabaseConfig instance for the default database" in {
      val dbConfProvider = injector.instanceOf[DatabaseConfigProvider]
      dbConfProvider.get must not(beNull)
    }
    "return the same DatabaseConfig instance for the default database" in {
      val dbConfProvider1 = injector.instanceOf[DatabaseConfigProvider]
      val dbConfProvider2 = injector.instanceOf[DatabaseConfigProvider]
      dbConfProvider1.get mustEqual dbConfProvider2.get
    }
    "return a DatabaseConfigProvider with a DatabaseConfig instance for the database named default" in {
      val binding =
        new BindingKey(classOf[DatabaseConfigProvider], Some(QualifierInstance(new NamedDatabaseImpl("default"))))
      val dbConfProvider = injector.instanceOf(binding)
      dbConfProvider.get must not(beNull)
    }
    "return the same DatabaseConfig instance for the database named default" in {
      val binding =
        new BindingKey(classOf[DatabaseConfigProvider], Some(QualifierInstance(new NamedDatabaseImpl("default"))))
      val dbConfProvider1 = injector.instanceOf(binding)
      val dbConfProvider2 = injector.instanceOf(binding)
      dbConfProvider1.get mustEqual dbConfProvider2.get
    }
    "return a DatabaseConfigProvider with a DatabaseConfig instance for a named (not default) database" in {
      val binding =
        new BindingKey(classOf[DatabaseConfigProvider], Some(QualifierInstance(new NamedDatabaseImpl("somedb"))))
      val dbConfProvider = injector.instanceOf(binding)
      dbConfProvider.get must not(beNull)
    }
    "return the same DatabaseConfig instance for a named database" in {
      val binding =
        new BindingKey(classOf[DatabaseConfigProvider], Some(QualifierInstance(new NamedDatabaseImpl("somedb"))))
      val dbConfProvider1 = injector.instanceOf(binding)
      val dbConfProvider2 = injector.instanceOf(binding)
      dbConfProvider1.get mustEqual dbConfProvider2.get
    }
  }
}
