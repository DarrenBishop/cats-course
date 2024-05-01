package typeclasses

import scala.concurrent.{Await, Future}
import cats.Monad

import absmath.Monads.MyMonad

object Semigroupals {

  trait MySemigroupal[F[_]] extends MyMonad[F] {
    def product[A, B](fa: F[A], fb:F[B]): F[(A, B)] =
      flatMap(fa)(a => map(fb)(b => (a, b)))
  }

  import cats.Semigroupal
  //import cats.instances.option._ // implicit Semigroupal[Option]
  val optionSemigroupal = Semigroupal[Option]
  val aTupledOption = optionSemigroupal.product(Some(123), Some("a string")) // Some((123, "a string"))
  val aNoneTupled = optionSemigroupal.product(Some(123), None) // None

  def runExamples(): Unit = {
    println(optionSemigroupal)
    println(aTupledOption)
    println(aNoneTupled)
    println()
  }

  implicit val ec: EC = EC()
  //import cats.instances.future._ // implicit Semigroupal[Future]
  val aTupledFuture = Semigroupal[Future].product(Future("the meaning of life"), Future(42)) // Future(("the meaning of life", 42))

  def runFuture(): Unit = {
    println(Await.result(aTupledFuture, 1.second))
    println()
  }

  //import cats.instances.list._ // implicit Semigroupal[List]
  val aTupledList = Semigroupal[List].product(List(1, 2), List("a", "b"))

  def runList(): Unit = {
    println(aTupledList) // List((1, "a"), (1, "b"), (2, "a"), (2, "b"))
    println()
  }

  // TODO
  import cats.syntax.flatMap._
  import cats.syntax.functor._
  def productWithMonads[F[_]: Monad, A, B](fa: F[A], fb: F[B]): F[(A, B)] = for {
    a <- fa
    b <- fb
  } yield (a, b)

  def runTodo(): Unit = {
    println(productWithMonads(Option(123), Option("a string")))
    println(productWithMonads(Option(123), None))
    println(Await.result(productWithMonads(Future("the meaning of life"), Future(42)), 1.second))
    println(productWithMonads(List(1, 2), List("a", "b")))
    println()
  }

  // INSIGHT: Monads extend Semigroupals
  // example of combination on non-monadic way: Validated
  import cats.data.Validated
  import cats.data.Validated._
  type ErrorsOr[T] = Validated[List[String], T]

  val validatedSemigroupal = Semigroupal[ErrorsOr] // requires implicit instance of Semigroup[List[_]]
  //val validatedSemigroupal = Semigroupal[Validated[List[String], _]] // with Kind-Projector compiler-plugin
  val invalidCombination = validatedSemigroupal.product(invalid(List("bad", "worse")), invalid(List("fubar")))
  val validCombination = validatedSemigroupal.product(valid(123), valid(456))

  def runValidated(): Unit = {
    println(invalidCombination) // Invalid(List("bad, "worse", "fubar"))
    println(validCombination) // Valid((123, 456))
    println()
  }

  type EitherErrorsOr[T] = Either[List[String], T]
  //import cats.instances.either._ // implicit Monad[EitherErrorOr]...
  val eitherSemigroupal = Semigroupal[EitherErrorsOr]
  //val eitherSemigroupal = Semigroupal[Either[List[String], _]] // with Kind-Projector compiler-plugin
  val a: Option[String] = none
  val b: Option[String] = "snafu".some
  // ...therefore product is implemented in terms of map and flatMap i.e short-circuiting
  val leftCombination = eitherSemigroupal.product(left(List("bad", "worse")), left(List("fubar")))
  val rightCombination = eitherSemigroupal.product(right(123), right(456))

  def runEither(): Unit = {
    println(a)
    println(b)
    println(leftCombination) // Left(List("bad, "worse", "fubar"))
    println(rightCombination) // Right((123, 456))
    println()
  }

  // Monad Associativity Law:
  //  m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))

  // TODO 2: define a Semigroupal[List] which does a zip

  implicit case object ZipListSemigroupal extends Semigroupal[List] {
    def product[A, B](fa: List[A], fb: List[B]): List[(A, B)] = fa.zip(fb)
  }

  val zippedLists = Semigroupal[List].product(List(1, 2), List("a", "b"))

  def runTodo2(): Unit = {
    println(zippedLists) // List((1, "a"), (2, "b"))
    println()
  }

  def main(args: Array[String]): Unit = {
    runExamples()

    runFuture()

    runList()

    runTodo()

    runValidated()

    runEither()

    runTodo2()

    ec.shutdown()
  }
}
