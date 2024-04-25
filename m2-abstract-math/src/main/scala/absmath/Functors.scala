package absmath

import scala.util.Try

object Functors {

  val aModifiedList = List(1, 2, 3).map(_ + 1) // List(2, 3, 4)
  val aModifiedOption = Option(2).map(_ + 1) // Some(3)
  val aModifiedTry = Try(42).map(_ + 1) // Success(43)

  // simplified definition
  trait MyFunctor[F[_]] {
    def map[A, B](initialValue: F[A])(f: A => B): F[B]
  }

  import cats.Functor
  import cats.instances.list._
  val listFunctor = Functor[List]
  val incrementedNumbers = listFunctor.map(List(1, 2, 3))(_ + 1) // List(2, 3, 4)

  import cats.instances.option._
  val optionFunctor = Functor[Option]
  val incrementedOption = optionFunctor.map(Option(2))(_ + 1) // Some(3)

  import cats.instances.try_._
  val tryFunctor = Functor[Try]
  val incrementedTry = tryFunctor.map(Try(2))(_ + 1) // Success(3)


  // generalizing the API
  def d10xList(list: List[Int]): List[Int] = list.map(_ * 10)
  def d10xOption(option: Option[Int]): Option[Int] = option.map(_ * 10)
  def d10xTry(attempt: Try[Int]): Try[Int] = attempt.map(_ * 10)

  // generalize
  def do10x[F[_]](container: F[Int])(implicit functor: Functor[F]): F[Int] = functor.map(container)(_ * 10)

  // TODO 1: define your own functor for a binary tree
  // hint: define an object that extends Functor[Tree]
  trait Tree[+T]
  object Tree {
    def leaf[T](value: T): Tree[T] = Leaf(value)
    def branch[T](value: T, left: Tree[T], right: Tree[T]): Tree[T] = Branch(value, left, right)
  }
  case class Leaf[+T](value: T) extends Tree[T]
  case class Branch[+T](value: T, left: Tree[T], right: Tree[T]) extends Tree[T]

  import Tree._

  implicit case object TreeFunctor extends Functor[Tree] {
    override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = fa match {
      case Leaf(value) => leaf(f(value))
      case Branch(value, left, right) => branch(f(value), map(left)(f), map(right)(f))
    }
  }

  // extension method - map
  import cats.syntax.functor._

  val tree = branch(40, branch(5, leaf(10), leaf(30)), leaf(20))
  val incrementedTree = tree.map(_ + 1)

  //TODO 2: write a shorter do10x method using extension methods
  def do10xShorter[F[_]: Functor](container: F[Int]): F[Int] = container.map(_ * 10)

  def main(args: Array[String]): Unit = {
    println(aModifiedList)
    println(aModifiedOption)
    println(aModifiedTry)

    println(incrementedNumbers)
    println(incrementedOption)
    println(incrementedTry)

    println(do10x(List(1, 2, 3)))
    println(do10x(Option(2)))
    println(do10x(Try(2)))
    println(do10x(branch(30, leaf(10), leaf(2))))
    println(incrementedTree)

    println(do10xShorter(List(1, 2, 3)))
    println(do10xShorter(Option(2)))
    println(do10xShorter(Try(2)))
    println(do10xShorter(branch(30, leaf(10), leaf(2))))
    println(do10xShorter(tree))
  }
}
