package rtj
package data

import scala.annotation.tailrec


object Writers {

  import cats.data.Writer
  // 1 - define them at the start
  val aWriter: Writer[List[String], Int] = Writer(List("Started something"), 45)

  // 2 - manipulate them with pure FP
  val anIncrementedWriter = aWriter.map(_ + 1) // value increases, logs stay the same
  val aLogsWriter = aWriter.mapWritten(_ :+ "Found something interesting") // value stays the same, logs change
  val aWriterWithBoth = aWriter.bimap(logs => logs :+ "Found something interesting", _ + 1) // both value and logs change
  val aWriterWithBoth2 = aWriter.mapBoth { (logs, value) => (logs :+ "Found something interesting", value + 1) }

  // flatMap
  val writerA = Writer(Vector("Log A1", "Log A2"), 10)
  val writerB = Writer(Vector("Log B1"), 40)
  val compositeWriter = for {
    va <- writerA
    vb <- writerB
  } yield va + vb

  // reset the logs
  val anEmptyWriter = aWriter.reset //clear the logs, keep the value

  // 3 - dump either the value or the logs
  val value = aWriter.value
  val logs = aWriter.written
  val (bothLogs, andValue) = aWriter.run

  // TODO 1: rewrite a function which "prints" things with writers
  def countAndSay(n: Int): Unit = {
    if (n <= 0) println("starting")
    else {
      countAndSay(n - 1)
      println(n)
    }
  }

  def countAndLog(n: Int): Writer[Vector[String], Int] = {
    //if (n == 0) Writer(Vector("starting"), 0)
    //else countAndLog(n - 1).mapBoth((logs, value) => (logs :+ s"$n", value + 1))
    @tailrec
    def aux(writer: Writer[Vector[String], Int]): Writer[Vector[String], Int] = {
      if (writer.value == 0) writer.mapWritten(logs => "starting" +: logs)
      else aux(writer.mapBoth((logs, value) => (value.toString +: logs, value - 1)))
    }
    aux(Writer(Vector(), n))
  }

  // Benefit #1: we work with pure FP

  // TODO 2:
  def naiveSum(n: Int): Int = {
    if (n <= 0) 0
    else {
      println(s"Now at $n")
      val lowerSum = naiveSum(n - 1)
      println(s"Computed sum(${n - 1}) = $lowerSum")
      lowerSum + n
    }
  }

  def loggingSum(n: Int): Writer[Vector[String], Int] = {
    //if (n == 0) Writer(Vector(s"Computed sum($n) = $n"), 0)
    if (n == 0) Writer(Vector(), 0)
    else for {
      _ <- Writer(Vector(s"Now at $n"), n)
      logSum <- loggingSum(n - 1)
      _ <- Writer(Vector(s"Computed sum(${n - 1}) = $logSum"), n)
    } yield logSum + n
  }

  def main(args: Array[String]): Unit = {
    implicit val ec: EC = EC()

    println(compositeWriter.run)
    println()

    countAndSay(10)
    println()

    countAndLog(10).written.foreach(println)
    println()

    // causes logs (println) to be interleaved between threads
    val fns1 = Future(naiveSum(20))
    val fns2 = Future(naiveSum(20))
    println()

    println(Await.result(Future.sequence(List(fns1, fns2)), scala.concurrent.duration.Duration.Inf))

    // Writer is side-effect free in the log accumulation, which can be obtained and processed at the end
    val fls1 = Future(loggingSum(20))
    val fls2 = Future(loggingSum(20))

    val List(ls1, ls2) = Await.result(Future.sequence(List(fls1, fls2)), scala.concurrent.duration.Duration.Inf)

    ec.shutdown()
    println()

    println("Logs 1")
    ls1.written.foreach(println)
    println(ls1.value)
    println()

    println("Logs 2")
    ls2.written.foreach(println)
    println(ls2.value)
  }
}
