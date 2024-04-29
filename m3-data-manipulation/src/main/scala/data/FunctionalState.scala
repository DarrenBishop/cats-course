package data

object FunctionalState {

  type MyState[S, A] = S => (S, A)

  import cats.data.State
  val countAndSay: State[Int, String] = State(curretCount => (curretCount + 1, s"Counted $curretCount"))
  val (eleven, conted10) = countAndSay.run(10).value
  //println(s"Eleven: $eleven, counted10: $conted10")
  // state = "iterative" computation

  // imperative
  var a =10
  a += 1
  val firstComputation = s"Incremented by 1, obtained $a"
  a *= 5
  val secondComputation = s"Multiplied by 5, obtained $a"

  // pure FP with states
  val firstTransformation = State((s: Int) => (s + 1, s"Incremented by 1, obtained ${s + 1}"))
  val secondTransformation = State((s: Int) => (s * 5, s"Multiplied by 5, obtained ${s * 5}"))
  val compositeTransformation = firstTransformation.flatMap { result1 =>
    secondTransformation.map { result2 =>
      (result1, result2)
    }
  }
  val compositeTransformationFor = for {
    r1 <- firstTransformation
    r2 <- secondTransformation
  } yield (r1, r2) // identical

  val func1 = (s: Int) => (s + 1, s"Incremented by 1, obtained ${s + 1}")
  val func2 = (s: Int) => (s * 5, s"Multiplied by 5, obtained ${s * 5}")
  val compositeFunc = func1.andThen {
    case (state, r1) => (r1, func2(state))
  }

  // TODO 1: an online store
  case class ShoppingCart(items: List[String], total: Double)

  def addToCart(item: String, price: Double): State[ShoppingCart, Double] = State { cart =>
    (ShoppingCart(item :: cart.items, cart.total + price), cart.total + price)
  }

  def runTodo1(): Unit = {

    val state1 = addToCart("Fender guitar", 500)
    val state2 = addToCart("Elixir strings", 19)
    val state3 = addToCart("Electric cable", 8)

    //val shopping = for {
    //  _ <- addToCart("Fender guitar", 500)
    //  _ <- addToCart("Elixir strings", 19)
    //  total <- addToCart("Electric cable", 8)
    //} yield total

    val shopping = state1.flatMap(_ => state2.flatMap(_ => state3.map(st3 => st3)))
    println(shopping.run(ShoppingCart(List(), 0)).value)
  }

  // TODO 2: pure mental gymnastics
  // returns a State data structure that, when run, will not change the state but will issue the value f(a)
  def inspect[A, B](f: A => B): State[A, B] = State { state => (state, f(state)) }

  // returns a State data structure that, when run, returns the value of that state and makes no changes
  def get[A]: State[A, A] = State { state => (state, state) }

  // return a State data structure that, when run, returns Unit and sets the state to that value
  def set[A](value: A): State[A, Unit] = State { _ => (value, ()) }

  // returns a State data structure that, when run, returns Unit and sets the state to f(state)
  def modify[A](f: A => A): State[A, Unit] = State { state => (f(state), ()) }

  def runTodo2(): Unit = {
    val program = for {
      initialState <- get[List[String]]
      _ <- set("Reset state" :: initialState)
      _ <- modify((st: List[String]) => st.map(_.toUpperCase))
      _ <- modify((st: List[String]) => "Another value" :: st)
      _ <- modify((st: List[String]) => "Yet another value" :: st)
      first <- inspect((st: List[String]) => st.head)
      finalState <- get
    } yield s"State has ${finalState.size} entries with first element $first"
    println(program.run(Nil).value)
    println()
  }

  // methods available
  import cats.data.State._

  def main(args: Array[String]): Unit = {
    //println(compositeTransformationFor.run(10).value)
    //println()
    //
    //println(compositeFunc(10))
    //println()

    runTodo1()

    runTodo2()
  }
}
