package alienbits

object Kleislis {

  val func1: Int => Option[String] = x => if (x % 2 == 0) Some(s"$x is even") else None
  val func2: Int => Option[Int] = x => Some(x * 3)
  // val func3 = func2 andThen func1

  val plainFunc1: Int => String = x => if (x % 2 == 0) s"$x is even" else s"$x is odd"
  val plainFunc2: Int => Int = _ * 3
  val plainFunc3: Int => String = plainFunc2 andThen plainFunc1

  def runPlainFunctions: Unit = {
    println(plainFunc3(2))
    println(plainFunc3(3))
    println()
  }

  import cats.data.Kleisli
  val func1K: Kleisli[Option, Int, String] = Kleisli(func1)
  val func2K: Kleisli[Option, Int, Int] = Kleisli(func2)
  val func3K: Kleisli[Option, Int, String] = func2K andThen func1K

  // convenience
  val multiply: Kleisli[Option, Int, Int] = func2K.map(_ * 2) // x => Option(...).map(_ * 2)
  val chain = func2K.flatMap(_ => func1K)


  def runKleisliFunctions: Unit = {
    println(func1K(2))
    println(func3K(4))
    println(multiply(4))
    println(chain(5))
    println(chain(6))
    println()
  }

  // TODO: what do you recognise in InterestingKleisli?
  import cats.Id
  type InterestingKleisli[A, B] = Kleisli[Id, A, B] // wrapper over A => Id[B]
  // hint
  val times2 = Kleisli[Id, Int, Int](_ * 2)
  val plus4 = Kleisli[Id, Int, Int](_ + 4)
  val composed = times2 andThen plus4
  val composed2 = times2.flatMap(_ => plus4)
  val composedF = times2.flatMap(t2 => plus4.map(p4 => t2 + p4))
  val composedFor = for {
    t2 <- times2
    p4 <- plus4
  } yield t2 + p4
  //... the pattern is dependency-injection; it's the general form of Reader[A, B] i.e. ReaderT[Id, A, B]

  def runTodo: Unit = {
    println(composed(2))
    println(composed2(2))
    println(composedF(2))
    println(composedFor(2))
  }

  def main(args: Array[String]): Unit = {
    runPlainFunctions

    runKleisliFunctions

    runTodo
  }
}