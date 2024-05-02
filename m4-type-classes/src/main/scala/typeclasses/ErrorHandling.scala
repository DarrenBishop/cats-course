package typeclasses

import scala.util.Try
import cats.{Applicative, Monad}

object ErrorHandling {

  trait MyApplicativeError[M[_], E] extends Applicative[M] {
    // pure from Applicative
    def raiseError[A](e: E): M[A]
    def handleErrorWith[A](ma: M[A])(func: E => M[A]): M[A]
    def handleError[A](ma: M[A])(func: E => A): M[A] = handleErrorWith(ma)(e => pure(func(e)))
  }

  trait MyMonadError[M[_], E] extends MyApplicativeError[M, E] with Monad[M]  {
    def ensure[A](ma: M[A])(error: => E)(predicate: A => Boolean): M[A] =
      flatMap(ma) { a => if (predicate(a)) pure(a) else raiseError(error) }
  }

  import cats.MonadError
  //import cats.instances.either._ // implicit MonadError
  type ErrorOr[A] = Either[String, A]
  val monadErrorEither: MonadError[ErrorOr, String] = MonadError[ErrorOr, String]
  val success: ErrorOr[Int] = monadErrorEither.pure(42) // Either[String, Int] == Right(42)
  val failure: ErrorOr[Int] = monadErrorEither.raiseError("something wrong") // Either[String, Int] == Left("something wrong")

  val handledError: ErrorOr[Int] = monadErrorEither.handleError(failure) {
    case "Badness" => 44
    case _ => 89
  }

  val handledError2: ErrorOr[Int] = monadErrorEither.handleErrorWith(failure) {
    case "Badness" => monadErrorEither.pure(44) // ErrorOr[Int]
    case _ => Left("something else") // ErrorOr[Int]
  }

  // filter
  val filteredSuccess: ErrorOr[Int] = monadErrorEither.ensure(success)("number too small")(_ > 100)

  def runExamples: Unit = {
    println(success)
    println(failure)
    println(handledError)
    println(handledError2)
    println(filteredSuccess)
    println()
  }

  // Try and Future
  import cats.instances.try_._ // implicit MonadError[Try], E = Throwable
  def exception = new RuntimeException("Really bad")
  val failedTry: Try[Int] = MonadError[Try, Throwable].raiseError(exception) // Try[Nothing] == Failure(exception)

  def runTry: Unit = {
    println(failedTry)
    println()
  }

  import cats.instances.future._ // implicit MonadError[Future], E = Throwable
  implicit val ec: EC = EC()
  val failedFuture = MonadError[Future, Throwable].raiseError(exception) // Future which will complete with a Failure(exception)

  def runFuture: Unit = {
    println(Await.ready(failedFuture, 1.second))
    println()
  }

  // Applicative => ApplicativeError
  import cats.data.Validated
  type ErrorsOr[T] = Validated[List[String], T]
  import cats.ApplicativeError
  //import cats.instances.list._ // implicit Semigroup[List] => ApplicativeError[ErrorOr, List[String]]
  val applicativeErrorValidated = ApplicativeError[ErrorsOr, List[String]]
  // pure, raiseError, handleError, handleErrorWith
  val aValid = applicativeErrorValidated.pure(42) // Validated[List[String], Int] == Valid(42)
  val anInvalid = applicativeErrorValidated.raiseError[Int](List("Badness")) // Validated[List[String], Int] == Invalid(List("Badness"))

  def runValidated: Unit = {
    println(aValid)
    println(anInvalid)
    println()
  }

  // extension methods from ApplicativeError
  import cats.syntax.applicative._ // pure
  import cats.syntax.applicativeError._ // raiseError, handleError, handleErrorWith
  val extendedSuccess = 42.pure[ErrorOr] // requires the implicit ApplicativeError[ErrorOr, List[String]]
  val extendedFailure = "Badness".raiseError[ErrorOr, Int]
  val extendedRecover = extendedFailure.recover {
    case _ => 43
  }

  def runExtendedApplicativeError: Unit = {
    println(extendedSuccess)
    println(extendedFailure)
    println(extendedRecover)
    println()
  }

  // extension methods from MonadError
  import cats.syntax.monadError._ // ensure
  val testedSuccess = success.ensure("something bad")(_ > 100)

  def runExtendedMonadError: Unit = {
    println(testedSuccess)
    println()
  }

  def main(args: Array[String]): Unit = {
    runExamples

    runTry

    runFuture

    runValidated

    runExtendedApplicativeError

    runExtendedMonadError

    ec.shutdown()
  }
}
