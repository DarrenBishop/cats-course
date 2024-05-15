package rtj
package typeclasses

import cats.{Eval, Monoid}

object Folding {

  // TODO: implement all in terms of foldLeft or foldRight
  object ListExercise {
    def map[A, B](list: List[A])(f: A => B): List[B] = list.foldRight(List.empty[B])((a, acc) => f(a) :: acc)
    def flatMap[A, B](list: List[A])(f: A => List[B]): List[B] = list.foldLeft(List.empty[B])((acc, a) => acc ::: f(a))
    def filter[A](list: List[A])(f: A => Boolean): List[A] = list.foldRight(List.empty[A])((a, acc) => if (f(a)) a :: acc else acc)
    //def filter[A](list: List[A])(f: A => Boolean): List[A] = flatMap(list)(a => if (f(a)) List(a) else Nil)
    def combineAll[A](list: List[A])(implicit M: Monoid[A]): A = list.foldLeft(M.empty)(M.combine)
  }

  def runListExercise: Unit = {
    import ListExercise._
    val list = List(1, 2, 3, 4, 5)
    println(map(list)(_ + 1)) // List(2, 3, 4, 5, 6)
    println(flatMap(list)(x => List(x, x + 1))) // List(1, 2, 2, 3, 3, 4, 4, 5, 5, 6)
    println(filter(list)(_ % 2 == 0)) // List(2, 4)
    println(combineAll(list)) // 15
    println()
  }

  import cats.Foldable
  //import cats.instances.list._ // implicit Foldable[List]
  val foldedList = Foldable[List].foldLeft(List(1, 2, 3), 0)(_ + _) // 6
  import cats.instances.option._ // implicit Foldable[Option]
  val foldedOption = Foldable[Option].foldLeft(Option(2), 30)(_ + _)

  // foldRight is stack-safe regardless of your container
  val foldedRight = Foldable[List].foldRight(List(1, 2, 3), Eval.now(0)) { (num, eval) => eval.map(_ + num) }  // 6

  // convenience methods in the presence of other type class instances
  val monoidFoldList = Foldable[List].fold(List(1, 2, 3)) // 6 // requires implicit Monoid[Int]
  val monoidCombinedList = Foldable[List].combineAll(List(1, 2, 3)) // 6 // requires implicit Monoid[Int]
  val monoidFoldedMapped = Foldable[List].foldMap(List(1, 2, 3))(_.toString) // "123" // requires implicit Monoid[String]

  def runFoldable: Unit = {
    println(foldedList)
    println(foldedOption)
    println(foldedRight.value)

    println(monoidCombinedList)
    println(monoidFoldedMapped)
    println()
  }

  val nestedInts = List(Vector(1, 2, 3), Vector(4, 5, 6))
  val combinedNested = (Foldable[List] compose Foldable[Vector]).combineAll(nestedInts)

  def runNested: Unit = {
    println(combinedNested) // 21
    println()
  }

  // extension methods
  import cats.syntax.foldable._
  val sum3 = List(1, 2, 3).combineAll // requires implicit Monoid[Int]
  val mapped = List(1, 2, 3).foldMap(_.toString) // requires implicit Monoid[String]

  def runExtensionMethods: Unit = {
    println(sum3)
    println(mapped)
    println()
  }

  def main(args: Array[String]): Unit = {
    runListExercise

    runFoldable

    runNested

    runExtensionMethods
  }
}
