package rtj
package absmath


object Monoids {

  import cats.Semigroup
  import cats.instances.int._
  import cats.syntax.semigroup._
  val numbers = (1 to 100).toList
  val strings = List("I'm ", "starting ", "to ", "like ", "monoids")

  // |+| is always associative
  val sumLeft = numbers.foldLeft(0)(_ |+| _)
  val sumRight = numbers.foldRight(0)(_ |+| _)

  // define a general API
  //def combineFold[T: Semigroup](list: List[T]): T = list.foldLeft(/* WHAT?! */)(_ |+| _)

  // Monoid
  import cats.Monoid
  val intMonoid = Monoid[Int]
  val combineInt = intMonoid.combine(23, 999) // 1022
  val zero = intMonoid.empty // 0

  import cats.instances.string._ // bring the implicit Monoid[String] in scope
  val emptyString = Monoid[String].empty
  val combineString = Monoid[String].combine("I understand ", "monoids") // "I understand monoids"

  import cats.instances.option._ // bring the implicit Monoid[Option[Int]] in scope
  import cats.syntax.option._
  val emptyOption = Monoid[Option[Int]].empty // None
  val combineOption = Monoid[Option[Int]].combine(Option(2), none) // Some(2)
  val combineOption2 = Monoid[Option[Int]].combine(Option(3), Option(6)) // Some(8)

  // extension methods from Monoid - |+|
  //import cats.syntax.monoid._ // either this one or cats.syntax.semigroup._
  val combinedOptionFancy = Option(3) |+| Option(7) // Some(10)

  // TODO 1: implement a combineFold
  def empty[T: Monoid]: T = Monoid[T].empty

  def combineFold[T: Monoid](list: List[T]): T = list.foldLeft(empty[T])(_ |+| _)

  // TODO 2: combine a list of phonebooks as Maps[String, Int]
  val phonebooks = List(
    Map("Alice" -> 235, "Bob" -> 647),
    Map("Charlies" -> 372, "Daniel" -> 889),
    Map("Tina" -> 123)
  )

  //implicit val phoneBookMonoid = Monoid.instance[Map[String, Int]](Map.empty, _ ++ _)
  import cats.instances.map._

  val massivePhonebook = combineFold(phonebooks)

  // TODO 3 - shopping cart and online stores with Monoids
  // hint: define your monoid - Monoid.instance
  case class ShoppingCart(items: List[String], total: Double)
  implicit val shoppingCartMonoid: Monoid[ShoppingCart] =
    Monoid.instance[ShoppingCart](
      ShoppingCart(empty[List[String]], empty[Double]),
      (sc1, sc2) => ShoppingCart(sc1.items |+| sc2.items, sc1.total |+| sc2.total)
    )
  def checkout(shoppingCarts: List[ShoppingCart]): ShoppingCart = combineFold(shoppingCarts)

  def main(args: Array[String]): Unit = {
    println(sumLeft)
    println(sumRight)

    println(combineFold(numbers))
    println(combineFold(strings))

    println(massivePhonebook)

    println(checkout(List(
      ShoppingCart(List("iPhone 12", "PlayStation 5"), 2000),
      ShoppingCart(List("Xbox Series X"), 400),
      ShoppingCart(List("Samsung Galaxy S20"), 800)
    )))
  }
}
