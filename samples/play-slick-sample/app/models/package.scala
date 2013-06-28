package object models{
  // Slick table objects should not be static singleton objects, because of a Scala bug. Use vals instead.
  val Cats = new Cats
}
