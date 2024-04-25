package absmath

import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object Monads {

  // lists
  val numberList = List(1, 2, 3)
  val charList = List('a', 'b', 'c')

  // TODO 1.1: how do you create all combinations of (number, char)?
  val listCombination = for {
    n <- numberList
    c <- charList
  } yield (n, c)
  val listCombinationDeSugared = numberList.flatMap(n => charList.map(c => (n, c)))

  // options
  val numberOption = Option(2)
  val charOption = Option('d')

  // TODO 1.2: how do you create the combinations of (number, char)?
  val optionCombination = for {
    n <- numberOption
    c <- charOption
  } yield (n, c)
  val optionCombinationDeSugared = numberOption.flatMap(n => charOption.map(c => (n, c)))
  val optionCombinationPatternMatch = (numberOption, charOption) match {
    case (Some(n), Some(c)) => Some((n, c))
    case _ => None
  }

  // futures
  val executorService: ExecutorService = Executors.newFixedThreadPool(8)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)
  val numberFuture = Future(42)
  val charFuture = Future('z')

  // TODO 1.3: how do you create the combinations of (number, char)?
  val futureCombination = for {
    n <- numberFuture
    c <- charFuture
  } yield (n, c)

  val futureCombinationSequence = Future.sequence(List(numberFuture, charFuture)).map {
    case List(n, c) => (n, c)
  }
  val futureCombinationOnComplete = numberFuture.flatMap { n => charFuture.map { c => (n, c) } }

  /*
    Pattern
    - wrapping a value into a M value
    - the flatMap mechanism

    Monads
   */

  // Cats Monad
  import cats.Monad
  //import cats.instances.option._ // brings the implicit Monad[Option] in scope
  val optionMonad = Monad[Option]
  val anOption = optionMonad.pure(4) // Option(4) == Some(4)
  val aTransformedOption = optionMonad.flatMap(anOption)(x => if (x % 3 == 0) Some(x + 1) else None) // None

  //import cats.instances.list._
  val listMonad = Monad[List]
  val aList = listMonad.pure(3) // List(3)
  val aTransformedList = listMonad.flatMap(aList)(x => List(x, x + 1)) // List(3, 4)

  // TODO 2: use a Monad[Future]
  //import cats.instances.future._
  val futureMonad: Monad[Future] = Monad[Future]
  val aFuture = futureMonad.pure(43) // Future(43)
  val aTransformedFuture = futureMonad.flatMap(aFuture)(x => futureMonad.pure(x + 44)) // Future(87)

  // specialized API
  def getPairsList(numbers: List[Int], chars: List[Char]): List[(Int, Char)] = numbers.flatMap(n => chars.map(c => (n, c)))
  def getPairOption(number: Option[Int], char: Option[Char]): Option[(Int, Char)] = number.flatMap(n => char.map(c => (n, c)))
  def getPairFuture(number: Future[Int], char: Future[Char]): Future[(Int, Char)] = number.flatMap(n => char.map(c => (n, c)))

  // generalized API
  def getPairs[M[_], A, B](ma: M[A], mb: M[B])(implicit M: Monad[M]): M[(A, B)] = M.flatMap(ma)(a => M.map(mb)(b => (a, b)))

  // extension methods - weirder imports: pure and flatMap
  import cats.syntax.applicative._ // brings pure in scope
  val oneOption = 1.pure[Option] // Some(1)
  import cats.syntax.option._
  val anotherOneOption = 1.some // Some(1)

  import cats.syntax.flatMap._ // brings flatMap in scope
  val oneOptionTransformed = oneOption.flatMap(x => (x + 1).some) // Some(2)

  // TODO 3: implement the map method in MyMonad[M[_]]
  trait MyMonad[M[_]] {
    def pure[A](value: A): M[A]
    def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
    // TODO 3 ...implement this
    def map[A, B](ma: M[A])(f: A => B): M[B] = flatMap(ma)(a => pure(f(a)))
  }

  // Key take-away: Monads are Functors
  val oneOptionMapped = Monad[Option].map(oneOption)(_ + 1) // Some(2)
  import cats.syntax.functor._
  val oneOptionMapped2 = oneOption.map(_ + 1) // Some(2)
  // for-comprehensions
  val composedOptionFor = for {
    one <- 1.pure[Option]
    two <- 2.pure[Option]
  } yield one + two

  // TODO 4: implement a shorter version of getPairs using for-comprehensions
  def getPairsFor[M[_]: Monad, A, B](ma: M[A], mb: M[B]): M[(A, B)] = for {
    a <- ma
    b <- mb
  } yield (a, b) // same as ma.flatMap(a => mb.map(b => (a, b))

  def main(args: Array[String]): Unit = {
    println(listCombination)
    println(listCombinationDeSugared)
    println()

    println(optionCombination)
    println(optionCombinationDeSugared)
    println(optionCombinationPatternMatch)
    println()

    import scala.concurrent.duration._
    println(Await.ready(futureCombination, 1.second))
    println(Await.ready(futureCombinationSequence, 1.second))
    println(Await.ready(futureCombinationOnComplete, 1.second))
    println()

    println(anOption)
    println(aTransformedOption)
    println()

    println(aList)
    println(aTransformedList)
    println()

    println(aFuture)
    println(aTransformedFuture)
    println()

    println(getPairs(numberList, charList))
    println(getPairs(numberOption, charOption))
    println(Await.ready(getPairs(numberFuture, charFuture), 1.second))
    println()

    println(getPairsFor(numberList, charList))
    println(getPairsFor(numberOption, charOption))
    println(Await.ready(getPairsFor(numberFuture, charFuture), 1.second))

    executorService.shutdown()
  }
}
