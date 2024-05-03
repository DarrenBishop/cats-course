package alienbits

import cats.Monoid

object InvariantFunctors {

  trait Crypto[A] { self =>
    def encrypt(value: A): String
    def decrypt(encrypted: String): A

    // declaring `map` first helps with type inferencing...
    // as A is already known, leaving B the only remaining type to infer
    def imap[B](map: A => B)(contramap: B => A): Crypto[B] = new Crypto[B] {
      def encrypt(value: B): String = self.encrypt(contramap(value))
      def decrypt(encrypted: String): B = map(self.decrypt(encrypted))
    }
  }

  def encrypt[A](value: A)(implicit C: Crypto[A]): String = C.encrypt(value)
  def decrypt[A](repr: String)(implicit C: Crypto[A]): A = C.decrypt(repr)

  implicit object CaesarCypher extends Crypto[String] {
    def encrypt(value: String): String = value.map(c => (c + 2).toChar)
    def decrypt(encrypted: String): String = encrypted.map(c => (c - 2).toChar)
  }

  def runString: Unit = {
    val cleartext = "Let's encrypt"
    println(cleartext)
    val encrypted = encrypt(cleartext)
    println(encrypted)
    val decrypted = decrypt[String](encrypted)
    println(decrypted)
    println()
  }

  /*
    How can we support ints, doubles, Option[String], etc?
   */
  implicit val doubleCrypto: Crypto[Double] = CaesarCypher.imap(_.toDouble)(_.toString)

  def runDouble: Unit = {
    println(Math.PI)
    val encryptedPi = encrypt(Math.PI)
    println(encryptedPi)
    val decryptedPi = decrypt[Double](encryptedPi)
    println(decryptedPi)
    println()
  }

  // TODO 1: support Option[String]
  implicit val stringOptionCrypto: Crypto[Option[String]] =
    CaesarCypher.imap(Option(_).filterNot(_.isBlank))(_.getOrElse(""))

  def runTodo: Unit = {
    val cleartext = "Let's encrypt"
    val cleartextOption = Option(cleartext)
    println(cleartextOption)
    val encrypted = encrypt(cleartextOption)
    println(encrypted)
    val decrypted = decrypt[Option[String]](encrypted)
    println(decrypted)
    println()

    println(encrypt[Option[String]](None))
    println(decrypt[Option[String]](""))
    println()
  }

  // TODO 2: generalize the pattern; given a Crypto[T] => Crypto[Option[T]], in the presence of Monoid[T]?
  implicit def optionCrypto[T](implicit C: Crypto[T], M: Monoid[T]): Crypto[Option[T]] =
    C.imap(Option(_).filterNot(_ == M.empty))(_.getOrElse(M.empty))

  def runTodo2: Unit = {
    val doubleOption = Option(Math.PI)
    println(doubleOption)
    val encrypted = encrypt(doubleOption)
    println(encrypted)
    val decrypted = decrypt[Option[Double]](encrypted)
    println(decrypted)
    println()

    val encryptedNoneDouble = encrypt[Option[Double]](None)
    println(s"`$encryptedNoneDouble`") // None => 0.0 => 202
    println(decrypt[Option[Double]](encryptedNoneDouble))
    println()
  }

  import cats.{Invariant, Show}
  //import cats.instances.string._ // implicit Show[String]
  val showString: Show[String] = Show[String]
  val showOptionString: Show[Option[String]] = Invariant[Show].imap(showString)(Option(_))(_.getOrElse(""))

  import cats.syntax.invariant._ // imap
  val showOptionStringShorter: Show[Option[String]] = showString.imap(Option(_))(_.getOrElse(""))

  // TODO 3: what's the relationship?
  trait MyInvariant[I[_]] {
    def imap[A, B](ia: I[A])(map: A => B)(contramap: B => A): I[B]
  }

  trait MyContravariant[C[_]] extends MyInvariant[C] {
    def contramap[A, B](ca: C[A])(func: B => A): C[B]

    def imap[A, B](ca: C[A])(map: A => B)(contramap: B => A): C[B] = this.contramap(ca)(contramap)
  }

  trait MyFunctor[F[_]] extends MyInvariant[F] { // a.k.a "covariant" functor
    def map[A, B](fa: F[A])(func: A => B): F[B]

    def imap[A, B](fa: F[A])(map: A => B)(contramap: B => A): F[B] = this.map(fa)(map)
  }

  def main(args: Array[String]): Unit = {
    runString

    runDouble

    runTodo

    runTodo2
  }
}
