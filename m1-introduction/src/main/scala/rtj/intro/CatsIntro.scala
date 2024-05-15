package rtj.intro

object CatsIntro {

  // Eq
  val aComparison = 2 == "a string" // wrong; will trigger a compiler warning, but will always evaluate to false

  // part 1
  import cats.Eq

  // part 2 - import TC (type-class) instances for type you need
  import cats.instances.int._ // including Eq[Int]

  // part 3 - use the TC API
  val intEquality = Eq[Int]
  val aTypeSafeComparison = intEquality.eqv(2, 3) // false
  //val anUnsafeComparison = intEquality.eqv(2, "a string") // boom! no compile

  // part 4 - use extension methods (if applicable)
  import cats.syntax.eq._
  val anotherTypeSafeComparison = 2 === 3 // false
  val neqComparison = 2 =!= 3 // true
  //val invalidComparison = 2 === "a string" // boom! no compile
  // extension methods are only visible in the presence of the right TC instances

  // part 5 - extending the TC operations to composite types e.g. lists
  import cats.syntax.list._
  val aListComparison = List(2) === List(2) // false

  // part 6 - creat a TC instance for a custom type
  case class ToyCar(model: String, price: Double)
  implicit val toyCarEq: Eq[ToyCar] = Eq.instance[ToyCar] { (car1, car2) =>
    car1.price == car2.price
  }

  val compareTwoToyCars = ToyCar("Ferrari", 29.99) === ToyCar("Lamborghini", 29.99) // true

  def main(args: Array[String]): Unit = {
    println(aTypeSafeComparison)
    println(anotherTypeSafeComparison)
    println(neqComparison)
    println(aListComparison)
    println(compareTwoToyCars)
  }
}
