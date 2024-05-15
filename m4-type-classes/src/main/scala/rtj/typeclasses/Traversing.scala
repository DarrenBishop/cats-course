package rtj
package typeclasses

import cats.{Applicative, Foldable, Functor, Monad, Semigroupal}


object Traversing {

  implicit val ec: EC = EC()

  val servers: List[String] = List("development", "staging", "production")
  def getBandwidth(hostname: String): Future[Int] = Future.successful(hostname.length * 100)

  /*
    we have
     - a List[String]
     - a func String => Future[Int]
    we want a Future[List[Int]]
   */
  val allBandwidths: Future[List[Int]] = servers.foldLeft(Future.successful(nil[Int])) { (acc, server) => getBandwidth(server).flatMap(bw => acc.map(_ :+ bw)) }
  //val sumBandwidths: Future[Int] = servers.foldLeft(Future.successful(0)) { (acc, server) => getBandwidth(server).flatMap(bw => acc.map(_ + bw)) }
  val sumBandwidths2: Future[Int] = servers.foldLeft(Future.successful(0)) { (acc, server) => for {
    all <-acc
    bw <- getBandwidth(server)
  } yield all + bw }

  val allBandwidthsTraverse: Future[List[Int]] = Future.traverse(servers)(getBandwidth)
  val allBandwidthsSequence: Future[List[Int]] = Future.sequence(servers.map(getBandwidth))

  def runFuture: Unit = {
    println(Await.result(allBandwidths, 1.second)) // 2800
    println(Await.result(allBandwidthsTraverse, 1.second)) // 2800
    println(Await.result(allBandwidthsSequence, 1.second)) // 2800
    println(Await.result(sumBandwidths2, 1.second)) // 2800
    println()
  }

  // TODO 1: implement traverse semantics
  def listTraverseM[F[_]: Monad, A, B](list: List[A])(f: A => F[B]): F[List[B]] = {
    import cats.syntax.applicative._
    import cats.syntax.flatMap._
    import cats.syntax.functor._
    list.foldLeft(nil[B].pure) { (facc, a) =>
      for {
        acc <- facc
        b <- f(a)
      } yield acc :+ b
    }
  }

  def listTraverse[F[_]: Applicative, A, B](list: List[A])(f: A => F[B]): F[List[B]] = {
    import cats.syntax.applicative._
    import cats.syntax.apply._
    list.map(f).foldLeft(nil[B].pure)((_, _).mapN[List[B]](_ :+ _))
  }

  def runTodo1: Unit = {
    println(Await.result(listTraverse(servers)(getBandwidth), 1.second))
    println()
  }

  // TODO 2: implement sequence semantics
  def listSequence[F[_]: Applicative, A](list: List[F[A]]): F[List[A]] = listTraverse(list)(identity)

  def runTodo2: Unit = {
    println(Await.result(listSequence(servers.map(getBandwidth)), 1.second))
    println()
  }

  // TODO 3: what's the result of
  val sequencedListOfVectors = listSequence(List(Vector(1, 2), Vector(3, 4)))
  val longerSequencedListOfVectors = listSequence(List(Vector(1, 2), Vector(3, 4), Vector(5, 6)))

  def runTodo3: Unit = {
    val result1 = Vector(Semigroupal[List].product(List(1, 2), List(3, 4)).map { case (a, b) => List(a, b) }: _*)
    println(s"result1 = $result1")
    println(s"sequencedListOfVectors = $sequencedListOfVectors")
    println(s"$result1 == sequencedListOfVectors: ${result1 == sequencedListOfVectors}")
    println()

    println(s"longerSsequencedListOfVectors = $longerSequencedListOfVectors")
    val sample = List(1, 3, 5)
    println(s"longerSsequencedListOfVectors contains $sample: ${longerSequencedListOfVectors.contains(sample)}")
    println()
  }

  def filterAsOption(list: List[Int])(p: Int => Boolean): Option[List[Int]] =
    listTraverse[Option, Int, Int](list)(Option(_).filter(p))

  // TODO 4: what's the result of
  val filteredAllEvens = filterAsOption(List(2, 4, 6))(_ % 2 == 0)
  val filteredSomeOdds = filterAsOption(List(1, 2, 3))(_ % 2 == 0)

  def runTodo4: Unit = {
    println(s"filteredEvens = $filteredAllEvens => $filteredAllEvens")
    println(s"filteredOdds = $filteredSomeOdds => $None")
    println()
  }

  import cats.data.Validated
  import cats.data.Validated.valid
  import cats.instances.list._ // implicit Semigroup[List] => Applicative[ErrorsOr]
  type ErrorsOr[T] = Validated[List[String], T]
  def filterAsValidated(list: List[Int])(p: Int => Boolean): ErrorsOr[List[Int]] =
    listTraverse(list)(valid(_).ensureOr(n => List(s"predicate failed for $n"))(p))

  // TODO 5: what's the result of
  val filteredAllEvensWithValidated = filterAsValidated(List(2, 4, 6))(_ % 2 == 0)
  val filteredSomeOddsWithValidated = filterAsValidated(List(1, 2, 3))(_ % 2 == 0)

  def runTodo5: Unit = {
    println(s"filteredAllEvensWithValidated = $filteredAllEvensWithValidated => Valid(List(2, 4, 6))")
    println(s"filteredSomeOddsWithValidated = $filteredSomeOddsWithValidated => Invalid(List(...1, ...3))")
    println()
  }

  trait MyTraverse[L[_]] extends Foldable[L] with Functor[L] {
    def traverse[F[_]: Applicative, A, B](la: L[A])(func: A => F[B]): F[L[B]]
    def sequence[F[_]: Applicative, A](la: L[F[A]]): F[L[A]] = traverse(la)(identity)

    // TODO 6: implement map
    def map[A, B](la: L[A])(f: A => B): L[B] = traverse[cats.Id, A, B](la)(f)
  }

  import cats.Traverse
  import cats.instances.future._ // implicit Applicative[Future]
  val allBandwidthsCats: Future[List[Int]] = Traverse[List].traverse(servers)(getBandwidth)

  def runTraverse: Unit = {
    println(Await.result(allBandwidthsCats, 1.second)) // 2800
    println()
  }

  // extension methods
  import cats.syntax.traverse._ // sequence + traverse
  val allBandwidthsCatsExtended: Future[List[Int]] = servers.traverse(getBandwidth)

  def runExtensionMethods: Unit = {
    println(Await.result(allBandwidthsCatsExtended, 1.second)) // 2800
    println()
  }

  def main(args: Array[String]): Unit = {
    runFuture

    runTodo1

    runTodo2

    runTodo3

    runTodo4

    runTodo5

    runTraverse

    runExtensionMethods

    ec.shutdown()
  }
}
