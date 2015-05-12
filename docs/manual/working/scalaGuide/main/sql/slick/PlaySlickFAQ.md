# Play Slick FAQ

## A binding to `play.api.db.DBApi` was already configured

If you get the following exception when starting your Play application:

```
1) A binding to play.api.db.DBApi was already configured at play.api.db.slick.evolutions.EvolutionsModule.bindings:
Binding(interface play.api.db.DBApi to ConstructionTarget(class play.api.db.slick.evolutions.internal.DBApiAdapter) in interface javax.inject.Singleton).
 at play.api.db.DBModule.bindings(DBModule.scala:25):
Binding(interface play.api.db.DBApi to ProviderConstructionTarget(class play.api.db.DBApiProvider))
```

It is very likely that you have [[enabled the jdbc plugin|ScalaDatabase]], and that doesn't really make sense if you are using Slick for accessing your databases. To fix the issue simply remove **jdbc** from your project's build `libraryDependencies`.

Another possibility is that there is another Play module that is binding [[DBApi|api/scala/index.html#play.api.db.DBApi]] to some other concrete implementation. This means that you are still trying to use Play Slick together with another Play module for database access, which is likely not what you want.
