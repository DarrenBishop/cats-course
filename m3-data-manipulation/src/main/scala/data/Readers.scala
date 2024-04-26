package data

object Readers {

  /*
    - configuration file => initial data structure
    - a DB layer
    - an HTTP layer
    - a business logic layer
   */
  case class Configuration(dbUsername: String, dbPassword: String, host: String, port: Int, threadPoolSize: Int, emailReplyTo: String)

  case class OrderStatus(orderId: Int, status: String)

  case class DbConnection(username: String, password: String) {
    // select * from the db table and return the status of the order
    def getOrderStatus(orderId: Int): OrderStatus = OrderStatus(orderId, "dispatched")

    // select max(orderId) from table where username = username
    def getLastOrderId(username: String): Int = 45
  }

  case class HttpService(host: String, port: Int) {
    // this would start the actual server
    def start(): Unit = println("Server started")
  }

  // bootstrap
  val config = Configuration("daniel", "rockthejvm1!", "localhost", 1234, 8, "store@rockthejvm.com")

  import cats.data.Reader
  val dbReader: Reader[Configuration, DbConnection] = Reader(conf => DbConnection(conf.dbUsername, conf.dbPassword))
  val dbConn = dbReader.run(config)

  // Reader[I, O]
  val danielsOrderStatusReader: Reader[Configuration, OrderStatus] = dbReader.map(dbConn => dbConn.getOrderStatus(55))

  def getLastOrderStatus(username: String): OrderStatus = {
    val userLastOrderStatusReader = dbReader
      .map(_.getLastOrderId(username))
      .flatMap(lastOrderId => dbReader.map(_.getOrderStatus(lastOrderId)))

    val userLastOrderStatusReaderFor = for {
      lastOrderId <- dbReader.map(_.getLastOrderId(username))
      orderStatus <- dbReader.map(_.getOrderStatus(lastOrderId))
    } yield orderStatus

    userLastOrderStatusReaderFor.run(config)
  }

  /*
    Pattern
    1. you create the initial data structure
    2. you create a reader which specifies how that data structure will be manipulated later
    3. you can then map and flatMap the reader to produce derived information
    4. when you need the final piece of information, you call run on the reader with the initial data structure
   */

  case class EmailService(emailReplyTo: String) {
    def sendEmail(address: String, status: OrderStatus): String =
      s"""From: $emailReplyTo;
         |To: $address
         |Subject : Order Status #${status.orderId}
         |
         |Your last order has the status: ${status.status}
         |""".stripMargin
  }

  val emailServiceReader: Reader[Configuration, EmailService] = Reader(conf => EmailService(conf.emailReplyTo))

  // TODO 1:  email user
  def emailUser(username: String, useremail: String): String = {
    // fetch the status of their last order
    // email them with the EmailService: "Your last order has the status: (status)"
    val task = for {
      lastOrderId <- dbReader.map(_.getLastOrderId(username))
      orderStatus <- dbReader.map(_.getOrderStatus(lastOrderId))
      email <- emailServiceReader.map(_.sendEmail(useremail, orderStatus))
    } yield email

    val task2 = for {
      db <- dbReader
      lastOrderId = db.getLastOrderId(username)
      orderStatus = db.getOrderStatus(lastOrderId)
      emailService <- emailServiceReader
    } yield emailService.sendEmail(useremail, orderStatus)

    task2.run(config)
  }

  // TODO 2: what programming pattern do Readers resemble?
  // Dependency Injection!

  def main(args: Array[String]): Unit = {
    println(danielsOrderStatusReader.run(config).status)
    println(getLastOrderStatus("daniel").status)
    println(emailUser("daniel", "daniel@rockthejvm.com"))
  }
}
