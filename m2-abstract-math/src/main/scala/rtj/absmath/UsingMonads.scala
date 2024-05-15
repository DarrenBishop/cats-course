package rtj
package absmath

import scala.util.Try


object UsingMonads {

  import cats.Monad
  //import cats.instances.list._
  val listMonad = Monad[List] // fetch the implicit Monad[List]
  val aSimpleList = listMonad.pure(2) // List(2)
  val anExtendedList = listMonad.flatMap(aSimpleList)(x => List(x, x + 1)) // List(2, 3)

  // applicable to Option, Try, Future

  // Either is also monadic
  val aManualEither: Either[String, Int] = Right(42)
  type LoadingOr[T] = Either[String, T]
  type ErrorOr[T] = Either[Throwable, T]
  val loadingMonad = Monad[LoadingOr]
  val aLoading = loadingMonad.pure(45) // LoadingOr[Int] == Right(45)
  val aChangedLoading = loadingMonad.flatMap(aLoading){ n => if (n % 2 ==0) Right(n + 1) else Left("loading meaning of life...") }

  // imaginary online store
  case class OrderStatus(orderId: Long, status: String)
  def getOrderStatus(orderId: Long): LoadingOr[OrderStatus] =
    Right(OrderStatus(orderId, "Ready to ship"))
  def trackLocation(orderStatus: OrderStatus): LoadingOr[String] =
    if (orderStatus.orderId > 1000) Left("Not available yet; refreshing data...")
    else Right("Amsterdam, NL")

  val orderId = 457L
  val orderLocation: LoadingOr[String] = loadingMonad.flatMap(getOrderStatus(orderId))(trackLocation)

  // use extension methods
  //import cats.syntax.flatMap._ // not needed as Either is monadic in the STD library
  //import cats.syntax.functor._ // not needed as Either is monadic in the STD library
  val orderLocationBetter: LoadingOr[String] = getOrderStatus(orderId).flatMap(trackLocation)

  val orderLocationFor: LoadingOr[String] = for {
    orderStatus <- getOrderStatus(orderId)
    location <- trackLocation(orderStatus)
  } yield location

  // TODO: the service layer API of a web app
  case class Connection(host: String, port: String)
  val config = Map(
    "host" -> "localhost",
    "port" -> "4040"
  )

  trait HttpService[M[_]] {
    def getConnection(cfg: Map[String, String]): M[Connection]

    def issueRequest(connection: Connection, payload: String): M[String]
  }

  def getResponse[M[_]: Monad](payload: String, service: HttpService[M]): M[String] = {
    import cats.syntax.functor._
    import cats.syntax.flatMap._
    for {
      conn <- service.getConnection(config)
      response <- service.issueRequest(conn, payload)
    } yield response
  }

  // DO NOT CHANGE THE CODE

  /*
    Requirements:
    - the `getConnection` method returns a M containing a Connection with those values,  if the host and port are
      found in the configuration map; otherwise, the method will fail, according to the logic of the type M:
      - For Try it will return a Failure
      - For Option it will return a None
      - For Future it will return a failed Future
      - For Either it will return a Left
    - the `issueRequest` method returns a M containing the string: "request (payload) has been accepted",
      if the payload is less than 20 characters; otherwise, the method will fail, according to the logic of the type M

      TODO: provide a real implementation of HttpService using Try, Option, Future, Either
   */

  def createTryService = new HttpService[Try] {
    def getConnection(cfg: Map[String, String]): Try[Connection] = for {
      h <- Try(cfg("host"))
      p <- Try(cfg("port"))
    } yield Connection(cfg("host"), cfg("port"))

    def issueRequest(connection: Connection, payload: String): Try[String] =
      Try(payload).collect { case p if p.length <= 20 => "request (payload) has been accepted" }
  }

  def createOptionService = new HttpService[Option] {
    def getConnection(cfg: Map[String, String]): Option[Connection] = for {
      h <- cfg.get("host")
      p <- cfg.get("port")
    } yield Connection(h, p)

    def issueRequest(connection: Connection, payload: String): Option[String] = {
      Option(payload).collect { case p if p.length <= 20 => "request (payload) has been accepted" }
    }
  }

  def createFutureService = new HttpService[Future] {
    def getConnection(cfg: Map[String, String]): Future[Connection] = {
      if (!cfg.contains("host"))
        Future.failed(new RuntimeException("Configuration is missing host"))
      else if (!cfg.contains("port"))
        Future.failed(new RuntimeException("Configuration is missing port"))
      else
        Future.successful(Connection(cfg("host"), cfg("port")))
    }

    def issueRequest(connection: Connection, payload: String): Future[String] = {
      if (payload.length > 20)
        Future.failed(new RuntimeException("Payload is too large"))
      else
        Future.successful("request (payload) has been accepted")
    }
  }

  def createEitherService = new HttpService[LoadingOr] {
    def getConnection(cfg: Map[String, String]): LoadingOr[Connection] = {
      if (!cfg.contains("host"))
        Left("Configuration is missing host")
      else if (!cfg.contains("port"))
        Left("Configuration is missing port")
      else
        Right(Connection(cfg("host"), cfg("port")))
    }

    def issueRequest(connection: Connection, payload: String): LoadingOr[String] = {
      if (payload.length > 20)
        Left("Payload is too large")
      else
        Right("request (payload) has been accepted")
    }
  }

  def main(args: Array[String]): Unit = {
    val payload = "Hello, Scala!"
    //val payload = "Hello, Scala!" + ("!" * 20)

    val tryService = createTryService
    println(getResponse(payload, tryService))
    println()

    val optionService = createOptionService
    println(getResponse(payload, optionService))
    println()

    implicit val ec: EC = EC()
    val futureService = createFutureService
    println(ready(getResponse(payload, futureService)))
    println()

    val loadingOrService = createEitherService
    println(getResponse(payload, loadingOrService))

    ec.shutdown()
  }
}
