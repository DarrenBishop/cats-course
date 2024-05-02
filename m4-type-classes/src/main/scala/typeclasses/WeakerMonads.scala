package typeclasses

import cats.{Applicative, Apply}

object WeakerMonads {

  trait MyFlatMap[M[_]] extends Apply[M] {
    def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B] // fundamental

    // TODO: implement ap using flatMap
    // hint: Apply extends Functor, so you can use map
    def ap[A, B](mf: M[A => B])(ma: M[A]): M[B] = flatMap(mf)(f => map(ma)(f))
  }

  trait MyMonad[M[_]] extends Applicative[M] with MyFlatMap[M]

  import cats.FlatMap
  import cats.syntax.flatMap._ // provides flatMap extension method
  import cats.syntax.functor._ // provides map extension method

  def getPairs[M[_]: FlatMap](numbers: M[Int], chars: M[Char]): M[(Int, Char)] = for {
    n <- numbers
    c <- chars
  } yield (n, c)

  def getGenericPairs[M[_]: FlatMap, A, B](ma: M[A], mb: M[B]): M[(A, B)] = for {
    a <- ma
    b <- mb
  } yield (a, b)

  def main(args: Array[String]): Unit = {
    println(getPairs(List(1, 2, 3), List('a', 'b', 'c')))
    println(getGenericPairs(List(1, 2, 3), List('a', 'b', 'c')))
  }
}
