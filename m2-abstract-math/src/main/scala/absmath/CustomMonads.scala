package absmath

import scala.annotation.tailrec

object CustomMonads {

  import cats.Monad
  implicit object OptionMonad extends Monad[Option] {
    override def pure[A](a: A): Option[A] = Option(a)

    override def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = fa.flatMap(f)

    @tailrec
    override def tailRecM[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] = f(a) match {
      case None => None
      case Some(Left(v)) => tailRecM(v)(f)
      case Some(Right(v)) => pure(v)
    }
  }

  // TODO 1: define a monad for the identity type
  type Identity[T] = T
  val aNumber: Identity[Int] = 42

  implicit object IdentityMonad extends Monad[Identity] {
    override def pure[A](a: A): Identity[A] = a

    override def flatMap[A, B](ia: Identity[A])(f: A => Identity[B]): Identity[B] = f(ia)

    @tailrec
    override def tailRecM[A, B](a: A)(f: A => Identity[Either[A, B]]): Identity[B] = f(a) match {
      case Left(v) => tailRecM(v)(f)
      case Right(v) => pure(v)
    }
  }

  // harder example
  sealed trait Tree[+A]
  final case class Leaf[+A](value: A) extends Tree[A]
  final case class Branch[+A](left: Tree[A], right: Tree[A]) extends Tree[A]
  object Tree {
    def leaf[A](value: A): Tree[A] = Leaf(value)
    def branch[A](left: Tree[A], right: Tree[A]): Tree[A] = Branch(left, right)
  }

  // TODO 2: define a monad for Tree
  implicit object TreeMonad extends Monad[Tree] {
    import Tree._
    override def pure[A](a: A): Tree[A] = Leaf(a)

    override def flatMap[A, B](ta: Tree[A])(f: A => Tree[B]): Tree[B] = ta match {
      case Leaf(value) => f(value)
      case Branch(left, right) => Branch(flatMap(left)(f), flatMap(right)(f))
    }

    override def tailRecM[A, B](a: A)(f: A => Tree[Either[A, B]]): Tree[B] = ???

    //override def tailRecM[A, B](a: A)(f: A => Tree[Either[A, B]]): Tree[B] = {
    //  type Trees = List[Tree[Either[A, B]]]
    //  type Branches = List[Branch[Either[A, B]]]
    //  type Dones = List[Tree[B]]
    //  @tailrec
    //  def aux(trees: Trees, branches: Branches, dones: Dones): Tree[B] = (trees, branches, dones) match {
    //    case (Nil, Nil, done :: Nil) => done
    //    case (Leaf(Right(b)) :: tail, bs, ds) => aux(tail, bs, leaf(b) :: ds)
    //    case (Leaf(Left(a)) :: tail , bs, ds) => aux(f(a) :: tail, bs, ds)
    //
    //    case ((brt @ Branch(_, _)) :: tail, Nil     , ds)                  => aux(brt.left :: brt.right :: brt :: tail, brt :: Nil, ds)
    //    case ((brt @ Branch(_, _)) :: tail, br :: bs, ds) if brt != br     => aux(brt.left :: brt.right :: brt :: tail, brt :: br :: bs, ds)
    //    case ( _                   :: tail, _ :: bs , left :: right :: ds) => aux(tail, bs, branch(left, right) :: ds)
    //  }
    //
    //  aux(List(f(a)), Nil, Nil)
    //}

    //@tailrec
    //override def tailRecM[A, B](a: A)(f: A => Tree[Either[A, B]]): Tree[B] = f(a) match {
    //  case Leaf(Right(b)) => leaf(b)
    //  case Branch(Leaf(Right(b1)), Leaf(Right(b2))) => branch(leaf(b1), leaf(b2))
    //  case Leaf(Left(a)) => tailRecM(a)(f)
    //  case Branch(Leaf(Left(a)), Leaf(Right(b))) => tailRecM(a)(_ => branch(f(a), leaf(Right(b))))
    //  case Branch(Leaf(Right(b)), Leaf(Left(a))) => tailRecM(a)(_ => branch(leaf(Right(b)), f(a)))
    //  case Branch(Leaf(Left(a1)), Leaf(Left(a2))) => tailRecM(a2)(_ => branch(f(a1), f(a2)))
    //}
  }

  def main(args: Array[String]): Unit = {
    val tree: Tree[Int] = Branch(Leaf(10), Branch(Leaf(20), Leaf(30)))
    val changedTree = TreeMonad.flatMap(tree)(x => Branch(Leaf(x + 1), Leaf(x + 2)))

    import Tree._
    println(changedTree)
    //println(TreeMonad.tailRecM(10) {
    //  case x if x % 2 == 0 => branch(leaf(Left(x / 2)), leaf(Left(x - x / 2)))
    //  case x => leaf(Right(s"odd $x"))
    //})
  }
}
