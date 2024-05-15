package rtj
package typeclasses


object Applicatives {

  // Applicatives = Functors + the `pure` method
  import cats.Applicative
  //import cats.instances.list._ // implicit Applicative[List]
  val listApplicative = Applicative[List]
  val aList = listApplicative.pure(2) // List(2)

  def runList: Unit = {
    println(listApplicative)
    println(aList)
    println()
  }

  //import cats.instances.option._ // implicit Applicative[Option]
  val optionApplicative = Applicative[Option]
  val anOption = optionApplicative.pure(2) // Some(2)

  def runOption: Unit = {
    println(optionApplicative)
    println(anOption)
    println()
  }

  object shadow {
    implicit def catsInstancesForId: Option[Unit] = Option(())
  }
  import shadow.catsInstancesForId

  // pure extension method
  import cats.syntax.applicative._
  val aSweetList = 2.pure[List] // List(2)
  val aSweetOption = 2.pure[Option] // Some(2)
  val aSweetEither: Either[String, Int] = 123.pure[Either[String, _]] // Right(123)

  def runExtensionMethods: Unit = {
    println(aSweetList)
    println(aSweetOption)
    println(aSweetEither)
    println()
  }

  // Monads extend Applicatives
  // Applicatives extend Functors
  import cats.data.Validated
  import cats.data.Validated._
  type ErrorsOr[T] = Validated[List[String], T]
  val aValidValue: ErrorsOr[Int] = 42.pure[ErrorsOr] // pure
  //val aValidValue: Validated[List[String], Int] = 42.pure[Validated[List[String], _]]
  val anotherValidValue: ErrorsOr[Int] = valid(42)
  val aModifiedValidValue = aValidValue.map(_ * 100) // map

  val validatedApplicative = Applicative[ErrorsOr]

  // TODO: thought experiment
  def productWithApplicative[W[_], A, B](wa: W[A], wb: W[B])(implicit ev: Applicative[W]): W[(A, B)] = {
    val wfb: W[B => (A, B)] = ev.map(wa)(a => (b: B) => (a, b))

    ev.ap(wfb)(wb)
  }

  // Applicative has this ap[W[_], A, B](wf: W[A => B])(wa: W[A]): W[B]
  // Applicative can implement `product` from Semigroupal using `ap`
  // => Applicative extends Semigroupal

  implicit val ec: EC = EC()

  def runTodo: Unit = {
    println(productWithApplicative(Option(123), Option("a string")))
    println(productWithApplicative(Option(123), None))
    println(Await.result(productWithApplicative(Future("the meaning of life"), Future(42)), 1.second))
    println(productWithApplicative(List(1, 2), List("a", "b")))
    println()
  }

  def runValidated: Unit = {
    println(aValidValue)
    println(anotherValidValue)
    println(s"Applicative.pure vs smart-constructor are ${if (aValidValue == anotherValidValue) "the same" else "borked!"}")
    println()
  }

  def main(args: Array[String]): Unit = {
    runList

    runOption

    runExtensionMethods

    runValidated

    runTodo

    ec.shutdown()
  }
}
