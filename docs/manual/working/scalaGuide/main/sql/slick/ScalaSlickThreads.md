# Using a separated execution context (thread pool)

You may want to use a separated execution context to avoid blocking threads in the default thread pool.
Play-slick provides a `DBAction` to handle this. In your controller, you can use `DBAction` as following :

```scala
import play.api.db.slick._
object Application extends Controller{
  def list(page: Int, orderBy: Int, filter: String) = DBAction { implicit rs =>
    ...
  }
}
```

N.B : you can use `DBAction("myDatabase")` if you need to use another datasource than "default".

Then you can configure the slick thread pool in your application.conf file :

```
play {
  akka {
    actor {
      slick-context = {
        fork-join-executor {
          parallelism-min = 300
          parallelism-max = 300
        }
      }
    }
  }
}
```

You can of course tune the number of threads, depending on your needs.
