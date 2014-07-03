##Multiple datasources and drivers

You can load a different datasource than the default, using the DB name parameter :

```scala
play.api.db.slick.DB("myOtherDb").withSession{ implicit session:Session =>
  Users.insert(User("fredrik","ekholdt"))
}
```

But what about the driver configuration if "myOtherDb" needs another database driver that the default one?
When having multiple datasources and drivers it is recommended to use the cake pattern.
Do not worry about the scary name it is quite easy.

Have a look in the [samples](https://github.com/playframework/play-slick/tree/master/samples) for an example of this or keep reading.

For each table you have, create a self-type of `play.api.db.slick.Profile` and import everything from the `profile` on your table:

@[component](code/ScalaSlickDrivers.scala)

Then you just have to put all your tables together into a DAO (data access object) like this:

@[dao](code/ScalaSlickDrivers.scala)

Whenever you need to use your database you just new up your DAO and import everything in it and in its profile:
    
@[current](code/ScalaSlickDrivers.scala)

This wil load the default database settings. 
If you want to use a custum datasource : 

```scala
import play.api.slick.DB
object current {
  val db = DB("mydb")
  val dao = new DAO(db.driver)      
} 
```

This will load the "mydb" database configuration (datasource and driver) from application.conf

Here is a configuration example for "mydb" : 

```conf
db.mydb.driver=com.mysql.jdbc.Driver
db.mydb.url="mysql://root:secret@localhost/myDatabase"
```

In your `application.conf` you can now do: `slick.default="models.current.*"`

You can then use it like this:

```scala
import current._
import play.api.slick.simple._

DB.withSession{ implicit session:Session => 
   Query(Users).list
}
```

Pweeh, there are a certain amount of lines of code there, but works great and scales along with the life cycle of your app: from the start, when you need tests, when you change the DB, ...

##Writing tests

To use a test datasource, you can use the `inMemoryDatabase` helper : 

```scala
import play.api.test.Helpers._

class DBSpec extends Specification {

  "DB" should {

   "my test" in new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        play.api.db.slick.DB.withSession{ implicit session:Session =>
          //...
      }
    }

  }
}

```
This will use a H2 datasource with this url : "jdbc:h2:mem:play-test"

Off course, you can also use the default datasource :

```scala
"default ds" in new WithApplication {
    play.api.db.slick.DB.withSession{ implicit session:Session =>
      //
}
```

Or a specific one : 

```scala
"specific ds" in new WithApplication {
  play.api.db.slick.DB("specific").withSession{ implicit session:Session =>
    //
}
```
