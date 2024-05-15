package rtj
package typeclasses

import cats.{Functor, Semigroupal}


object WeakerApplicatives {

  trait MyApply[F[_]] extends Functor[F] with Semigroupal[F] {
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B] = !!! // fundamental

    def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
      ap(map(fa)(a => (b: B) => (a, b)))(fb)

    def product[A, B, C](fa: F[A], fb: F[B], fc: F[C]): F[(A, B, C)] =
      ap(ap(map(fa)(a => (b: B) => (c: C) => (a, b, c)))(fb))(fc)

    // TODO: implement mapN
    def mapN[A, B, R](tf: (F[A], F[B]))(f: (A, B) => R): F[R] =
      map(product(tf._1, tf._2)) { t => f(t._1, t._2) }
    def mapN[A, B, C, R](tf: (F[A], F[B], F[C]))(f: (A, B, C) => R): F[R] =
      map(product(tf._1, tf._2, tf._3)) { t => f(t._1, t._2, t._3) }
  }

  trait MyApplicative[F[_]] extends MyApply[F] {
    def pure[A](a: A): F[A] // fundamental
  }

  import cats.Apply
  //import cats.instances.option._ // implicit Apply[Option]
  val applyOption = Apply[Option]
  val funcApp = applyOption.ap(Some((x: Int) =>  x + 1))(Some(2)) // Some(3)

  import cats.syntax.apply._
  val tupleOfOptions = (Option(1), Option(2), Option(3))
  val optionOfTuple = tupleOfOptions.tupled // Some((1,2,3))
  val sumOption = tupleOfOptions.mapN(_ + _ + _) // Some(6)
  val anotherTupleOfOptions = (Option(1), Option(2), none[Int])
  val multiplyOption = anotherTupleOfOptions.mapN(_ * _ * _) // None

  def runExamples: Unit = {
    println(funcApp)
    println(tupleOfOptions)
    println(optionOfTuple)
    println(sumOption)
    println(anotherTupleOfOptions)
    println(multiplyOption)
    println()
  }

  def main(args: Array[String]): Unit = {
    runExamples
  }
}
