package alienbits

import cats.Monoid

object ContravariantFunctors {

  // A contravariant type class, identified by the presence of a contramap method
  trait Format[T] { self =>
    def format(t: T): String
    def contramap[A](func: A => T): Format[A] = a => format(func(a))
  }

  def format[A](value: A)(implicit F: Format[A]): String = F.format(value)

  implicit object StringFormat extends Format[String] {
    def format(value: String): String = s""""$value""""
  }

  implicit object IntFormat extends Format[Int] {
    def format(value: Int): String = value.toString
  }

  implicit object BooleanFormat extends Format[Boolean] {
    def format(value: Boolean): String = if (value) "Y" else "N"
  }

  // problem: given a Format[T], can we have a Format[Option[T]]?
  implicit def getOptionFormat[T](implicit F: Format[T], M: Monoid[T]): Format[Option[T]] =
    //(value: Option[T]) => F.format(value.get)
    //contramap(_.get)
    F.contramap(_.getOrElse(M.empty))

  def contramap[A, T](func: A => T)(implicit F: Format[T]): Format[A] = a => F.format(func(a))

  /*
    fi: Format[Int] i.e. IntFormat above
    fo1: Format[Option[Int](_.get) // first get
    fo2: Format[Option[Option[Int]]](_.get) // second get

    fo2 = fi
      .contramap[Option[Int](_.get) // first get
      .contramap[Option[Option[Int]]](_.get) // second get

    fo2.format(Option(Option(42))) =
      fo1.format(secondGet(Option(Option(42))) =
        fi.format(firstGet(secondGet(Option(Option(42)))))

    order = REVERSE from the written order
    - second get
    - first get
    - format of Int

    Map applies transformations in sequence
    Contramap applies transformations in REVERSE sequence
   */

  import cats.Contravariant
  import cats.Show
  //import cats.instances.int._ // implicit Show[Int]
  /*implicit*/ val showInts: Show[Int] = Show[Int]
  /*implicit*/ val showOptionInts: Show[Option[Int]] = Contravariant[Show].contramap(showInts)(_.getOrElse(0))

  // extension methods
  import cats.syntax.contravariant._ // contramap
  import cats.syntax.show._ // contramap
  implicit def showOptionIntsShorter(implicit S: Show[Int]): Show[Option[Int]] = S.contramap(_.getOrElse(0))

  def main(args: Array[String]): Unit = {

    println(format(true))
    println(format("Nothing weird so far"))
    println(format(Option("Nothing weird so far")))
    println(format(Option(Option("Nothing weird so far"))))
    println(format(42))
    println(format(Option(42)))
    println(format(Option(Option(42))))
    println(format(Option(Option(Option(42)))))
    println()

    println(42.show)
    println(Option(42).show)
    println(Option(Option(42)).show)
    println(Option(Option(Option(42))).show)
  }
}
