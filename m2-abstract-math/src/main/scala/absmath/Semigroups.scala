package absmath

import cats.syntax.option._

object Semigroups {

  // Semigroups COMBINE elements of the same type
  import cats.Semigroup
  import cats.instances.int._
  val naturalInSemigroup = Semigroup[Int]
  val intCombination = naturalInSemigroup.combine(2, 46) // addition

  import cats.instances.string._
  val naturalStringSemigroup = Semigroup[String]
  val stringCombination = naturalStringSemigroup.combine("I love", " cats") // concatenation

  // specific API
  def reduceInts(list: List[Int]): Int = list.reduce(naturalInSemigroup.combine)
  def reduceStrings(list: List[String]): String = list.reduce(naturalStringSemigroup.combine)

  // general API
  def reduceThings[T](list: List[T])(implicit semigroup: Semigroup[T]): T = list.reduce(semigroup.combine)

  // TODO 1: support a new type
  case class Expense(id: Long, amount: Double)
  implicit val expenseSemigroup: Semigroup[Expense] = (a, b) => Expense(a.id max b.id, Semigroup[Double].combine(a.amount, b.amount))
  //implicit val expenseSemigroup = Semigroup.instance[Expense]((a, b) => Expense(a.id max b.id, Semigroup[Double].combine(a.amount, b.amount)))

  // extension methods from Semigroup
  import cats.syntax.semigroup._
  val anIntSum = 2 |+| 3 // requires the presence of an implicit Semigroup[Int]
  val aStringConcat = "we like " |+| "semigroups" // requires the presence of an implicit Semigroup[String]
  val aCombinedExpense = Expense(4, 80) |+| Expense(56, 46) // requires the presence of an implicit Semigroup[Expense]

  // TODO 2: implement reduceThings2 that works with the |+|
  def reduceThings2[T](list: List[T])(implicit semigroup: Semigroup[T]): T = list.reduce(_ |+| _)
  def reduceThings3[T: Semigroup](list: List[T]): T = list.reduce(_ |+| _)

  def main(args: Array[String]): Unit = {
    println(intCombination)
    println(stringCombination)

    // using the specific API
    val numbers = (1 to 10).toList
    println(reduceInts(numbers))

    val strings = List("I'm ", "starting ", "to ", "like ", "semigroups")
    println(reduceStrings(strings))

    // using the general API
    println(reduceThings(numbers)) // compiler injects the implicit Semigroup[Int]

    println(reduceThings(strings)) // compiler injects the implicit Semigroup[String]

    import cats.instances.option._
    // compiler will produce an implicit Semigroup[Option[Int]] - combine will produce another option with summmed elements
    // compiler will produce an implicit Semigroup[Option[String]] - combine will produce another option with concatenated elements
    // same for any type with an implicit Semigroup
    val numbersOption: List[Option[Int]] = none :: 45.some :: none ::  numbers.map(Option(_))
    println(reduceThings(numbersOption)) // an Option[Int] containing the sum of all numbers

    val stringsOption: List[Option[String]] = strings.map(Option(_))
    println(reduceThings(stringsOption)) // an Option[String] containing the concatenation of all strings

    // test ex 1
    val expenses = List(Expense(1, 99), Expense(2, 35), Expense(43, 10))
    println(reduceThings(expenses))

    // test ex 2
    println(reduceThings2(expenses))
    println(reduceThings3(expenses))
  }
}
