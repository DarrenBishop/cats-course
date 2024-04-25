package absmath

import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object MonadTransformers {

  def sumAllOptions(values: List[Option[Int]]): Int = ???

  // Option Transformer
  import cats.data.OptionT
  import cats.instances.list._ // fetch an implicit OptionT[List]

  val listOfIntOptions: OptionT[List, Int] = OptionT(List(Option(1), Option(2)))
  val listOfCharOptions: OptionT[List, Char] = OptionT(List(Option('a'), Option('b'), Option.empty))
  val listOfTuples: OptionT[List, (Int, Char)] = for {
    c <- listOfCharOptions
    n <- listOfIntOptions
  } yield (n, c)

  val executorService: ExecutorService = Executors.newFixedThreadPool(8)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

  // Either Transformer
  import cats.data.EitherT
  val listOfEithers: EitherT[List, String, Int] = EitherT(List(Left("Bad"), Right(42), Right(24)))
  val futureOfEither: EitherT[Future, String, Int] = EitherT.right(Future(42))

  /*
    TODO exercise
    We have a multi-machine cluster for your business which will receive a traffic surge following a media appearance.
    We measure bandwidth in units
    We want to allocate TWO of our servers to cope with the traffic spike.
    We know the current capacity for each server and we know we'll hold the traffic if the sum of bandwidth is > 250.
   */
  val bandwidths = Map(
    "server1" -> 50,
    "server2" -> 300,
    "server3" -> 170
  )

  type Response[T] = EitherT[Future, String, T]
  object Response {
    def apply[T](value: => T): Response[T] = EitherT.right(Future(value))
    def of[T](value: => T): Response[T] = apply(value)
    def fail[T](message: String): Response[T] = EitherT.left(Future(message))
  }

  def getBandwidth(server: String): Response[Int] = bandwidths.get(server) match {
    case None => Response.fail(s"Server $server unreachable")
    case Some(bandwidth) => Response.of(bandwidth)
  }

  // TODO 1
  // hint: call getBandwidth twice and combine the results
  def canWithstandSurge(server1: String, server2: String): Response[Boolean] = for {
    bandwidth1 <- getBandwidth(server1)
    bandwidth2 <- getBandwidth(server2)
  } yield bandwidth1 + bandwidth2 > 250

  // TODO 2
  // hint: call canWithstandSurge + transform
  def generateTrafficSpikeReport(server1: String, server2: String): Response[String] =
    canWithstandSurge(server1, server2).transform {
      case Left(message) => Left(message)
      case Right(false) => Left(s"Servers $server1 and $server2 cannot withstand the surge")
      case Right(true) => Right(s"Servers $server1 and $server2 can withstand the surge")
    }

  def main(args: Array[String]): Unit = {
    println(listOfTuples.value) // List((1, a), (2, a), (1, b), (2, b), None)

    bandwidths.keys.toList.combinations(2).foreach {
      case List(s1, s2) => println(Await.ready(generateTrafficSpikeReport(s1, s2).value, 1.second))
    }

    executorService.shutdown()
  }
}
