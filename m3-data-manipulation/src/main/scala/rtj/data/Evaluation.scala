package rtj
package data


object Evaluation {

  /*
    Cats make the distinction between
    - evaluating an expression eagerly
    - evaluating lazily and every time you request it
    - evaluating lazily and keeping the result i.e. memoization
   */

  import cats.Eval
  val instanceEval: Eval[Int] = Eval.now {
    println("Computing now!")
    1111
  }

  val redoEval: Eval[Int] = Eval.always {
    println("Computing again!")
    2222
  }

  val delayedEval: Eval[Int] = Eval.later {
    println("Computing later!")
    3333
  }

  val composedEval = instanceEval.flatMap(value1 => delayedEval.map(value2 => value1 + value2))

  // identical
  val anotherComposedEval = for {
    value1 <- instanceEval
    value2 <- delayedEval
  } yield value1 + value2

  def runSequencing(): Unit = {
    println(instanceEval.value)
    println(instanceEval.value)
    println(redoEval.value)
    println(redoEval.value)
    println()

    println(delayedEval.value)
    println(delayedEval.value)
    println()

    println(composedEval.value)
    println()
  }

  // TODO 1: predict the output
  val evalEx1 = for {
    a <- delayedEval
    b <- redoEval
    c <- instanceEval
    d <- redoEval
  } yield a + b + c + d

  def runTodo1(): Unit = {
    println(evalEx1.value)
    // prints Computing now!
    // prints Computing later!
    // prints Computing again!
    // prints Computing again!
    // prints 8888
    println(evalEx1.value)
    // prints Computing again!
    // prints Computing again!
    // prints 8888
    println()
  }

  // "remember" a computed value
  val dontRecompute = redoEval.memoize // Eval[Int]

  def runDontCompute(): Unit = {
    println(dontRecompute.value)
    println(dontRecompute.value)
    println()
  }

  val tutorial = Eval
    .always { println("Step 1"); "put the guitar on your lap" }
    .map { step1 => println("Step 2"); s"$step1 then put your left hand on the neck" }
    .memoize // remember the value up to this point
    .map { steps1and2 => println("Step 3, more complicated"); s"$steps1and2 then with the right hand strike the strings" }

  def runTutorial(): Unit = {
    println(tutorial.value)
    println(tutorial.value)
    println()
  }

  // TODO 2: implement defer such taht defer(Eval.now) does NOT run the side effects
  //def defer[T](eval: => Eval[T]): Eval[T] = for {
  //  _ <- Eval.later(())
  //  value <- eval
  //} yield value
  //def defer[T](eval: => Eval[T]): Eval[T] = Eval.later(eval.value)
  //def defer[T](eval: => Eval[T]): Eval[T] = Eval.later.flatMap(_ => eval)
  import cats.syntax.flatMap._
  def defer[T](eval: => Eval[T]): Eval[T] = Eval.Unit >> eval

  def runDefer(): Unit = {
    val deferred: Eval[Int] = defer {
      Eval.now {
        println("Now!")
        42
      }
    }

    println(deferred.value)
    //println(deferred.value)
    println()
  }

  // TODO 3: rewrie the method with Evals
  def reverseList[T](ts: List[T]): List[T] =
    if (ts.isEmpty) ts
    else reverseList(ts.tail) :+ ts.head

  def reverseListEval[T](ts: List[T]): Eval[List[T]] = {
    if (ts.isEmpty) Eval.now(ts)
    else
      defer { reverseListEval(ts.tail).map { rts => rts :+ ts.head }}
  }

  def runTodo3(): Unit = {
    println(reverseListEval((1 to 10000).toList).value)
    println()
  }

  def main(args: Array[String]): Unit = {
    //runSequencing()

    //runTodo1()

    //runDontCompute()

    //runTutorial()

    //runDefer()

    runTodo3()
  }
}
