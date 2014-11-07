# Keeping tables separated

If you have multiple tables depending on each other you will want to add a layer that helps manage these dependencies. For example, imagine you have two tables called Decks and Details. A deck has details so you will typically have to extract details from decks at different places in your application.


```scala
//deck.scala:
object Decks extends Table[Deck]("DECK") {
}

//detail.scala
object Details extends Table[Detail]("DETAILS") {
}
```

Follow the steps below to do this:

## Step 1 ##
Create an ImplicitSession trait like this:

```scala
//dal.scala:
trait ImplicitSession {
  implicit val implicitSession: Session
}
```

We will use this later to inject a Session into our methods.

## Step 2 ##
Create a trait that has the ImplicitSession as a self type. The self-type will contain the session.

```scala
trait Decks { this: ImplicitSession =>
  def getDetails(deck: Deck): List[Detail] = (for {
    //...
  } yield ( main, side )).first //or list ... NOTE: the implicit session is from ImplicitSession trait
```


## Step 3 ##
The Bridges object will contain methods that will use the ImplicitSession trait like this:

```scala
// in dal.scala
object Bridges {
  def Decks(implicit session: Session) = new ImplicitSession with Decks { override val implicitSession = session }
}
```

## Step 4 ##
On usage you will only have to import Bridges._ and you can use the methods as normal.

```scala
import dal.Bridges._

DB.withSession{ implicit session: Session => //or withTransaction if there are multiple operations
  val details = Decks.getDetails(id)
}

```

## Pros/cons ##

The pros:

* compared to using Slicks threadLocalSession you will risk getting no runtime errors
* no (implicit session: Session) on all methods
* an arguably clean way of separating the definition of the table in DB and the methods you need to use
* you can specify the session or transaction level and easily combine more calls to the DB in a session or transaction with out having to nest the levels

The cons:

* you have the extra BusinessObjects object
* you have to specify the session or transaction elsewhere

Note that this is not strictly related to play-slick, but is general to Slick
