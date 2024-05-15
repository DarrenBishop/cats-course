import cats.Monoid


package object rtj extends ec.Syntax { pkg =>

  def !!![T]: T = ???

  trait <:!<[-A, +B]
  // These are obsolete with Kind-Projector compiler-plugin
  //type IsNot[T] = {type λ[α] = <:!<[α, T]}
  //type <:![T] = IsNot[T]
  implicit def isNotSubClass[A, B]: A <:!< B = new <:!<[A, B] {}
  //implicit def isNotSubClassAmbig1[A, B](implicit ev: A <:< B): A <:!< A = isNotSubClass
  //implicit def isNotSubClassAmbig2[A, B](implicit ev: A <:< B): A <:!< A = isNotSubClass
  implicit def isNotSubClassAmbig1[A, B: A <:< _]: A <:!< A = isNotSubClass
  implicit def isNotSubClassAmbig2[A, B: A <:< _]: A <:!< A = isNotSubClass

  // Option support
  def none[T]: Option[T] = None
  def some[T](value: T): Option[T] = Some(value)

  // Either support
  def left[L, R](value: L): Either[L, R] = Left(value)
  def right[L, R](value: R): Either[L, R] = Right(value)

  // List support
  def nil[E] = List.empty[E]

  // Support for emptiness via Monoid
  def empty[T: Monoid]: T = Monoid[T].empty
}
