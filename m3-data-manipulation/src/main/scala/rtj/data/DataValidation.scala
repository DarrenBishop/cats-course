package rtj
package data


import scala.util.Try
import cats.kernel.Monoid

object DataValidation {

  import cats.data.Validated
  val aValidValue: Validated[String, Int] = Validated.valid(42) // "right" (desirable) value
  val anInvalidValue: Validated[String, Int] = Validated.invalid("Something went wrong") // "left" (undesirable) value
  val aTest: Validated[String, Int] = Validated.cond(42 > 39, 99, "meaning of life is too small")

  // TODO: use Either
  /*
    - n must be a prime
    - n must be non-negative
    - n <= 100
    - n must be even
   */
  def testNumber(n: Int): Either[List[String], Int] = {
    val isPrime: Int => Option[String] = n => Option("n must be a prime").filterNot { _ => ! ((2 until n) exists (n % _ == 0)) }
    val isNonNegative: Int => Option[String] = n => Option("n must be non-negative").filterNot { _ => n >= 0 }
    val isLessThan100: Int => Option[String] = n => Option("n <= 100").filterNot { _ => n < 100 }
    val isEven: Int => Option[String] = n => Option("n must be even").filterNot { _ => n % 2 == 0 }

    List(isPrime, isNonNegative, isLessThan100, isEven) flatMap { f => f(n) } match {
      case Nil => Right(n)
      case errors => Left(errors)
    }
  }

  def testNumber2(n: Int): Either[List[String], Int] = {
    val isPrime: List[String] = if (! ((2 until n) exists (n % _ == 0))) Nil else List("n must be a prime")
    val isNonNegative: List[String] = if (n >= 0) Nil else List("n must be non-negative")
    val isLessThan100: List[String] = if (n < 100) Nil else List("n <= 100")
    val isEven: List[String] = if (n % 2 == 0) Nil else List("n must be even")

    List(isPrime, isNonNegative, isLessThan100, isEven).flatten match {
      case Nil => Right(n)
      case errors => Left(errors)
    }
  }

  def validateNumber(n: Int): Validated[List[String], Int] = {
    import cats.syntax.semigroup._
    implicit val intMonoid: Monoid[Int] = Monoid.instance(0, _ max _)
    (
      Validated.cond(! ((2 until n) exists (n % _ == 0)), n, List("n must be a prime")) |+|
        Validated.cond(n >= 0, n, List("n must be non-negative")) |+|
        Validated.cond(n < 100, n, List("n <= 100")) |+|
        Validated.cond(n % 2 == 0, n, List("n must be even"))
      )//.map(_ => n)
  }

  def validateNumber2(n: Int): Validated[List[String], Int] = {
    import cats.syntax.semigroup._
    import cats.data.Validated.Valid
    implicit val intMonoid: Monoid[Int] = Monoid.instance(0, _ max _)
    (
      Valid(n).ensure(List("n must be a prime"))(v => ! ((2 until v) exists (v % _ == 0))) |+|
        Valid(n).ensure(List("n must be non-negative"))(v => v >= 0) |+|
        Valid(n).ensure(List("n <= 100"))(v => v < 100) |+|
        Valid(n).ensure(List("n must be even"))(v => v % 2 == 0)
      )//.map(_ => n)
  }

  def runTodo(): Unit = {
    println(testNumber(2))
    println(testNumber(101))
    println(testNumber(-101))
    println()

    println(testNumber2(2))
    println(testNumber2(101))
    println(testNumber2(-101))
    println()

    println(validateNumber(2))
    println(validateNumber(101))
    println(validateNumber(-101))
    println()

    println(validateNumber2(2))
    println(validateNumber2(101))
    println(validateNumber2(-101))
    println()
  }

  // chain
  aValidValue.andThen(_ => anInvalidValue)
  // test a valid value
  aValidValue.ensure(List("something went wring"))(_ % 2 == 0)
  // transform
  aValidValue.map(_ + 1)
  aValidValue.leftMap(_.length)
  aValidValue.bimap(_.length, _ + 1)
  // interoperate with stdlib
  val eitherToValidated: Validated[List[String], Int] = Validated.fromEither(Right(42))
  val optionToValidated: Validated[List[String], Int] = Validated.fromOption(None, List("Nothing present here"))
  val tryToValidated: Validated[Throwable, Int] = Validated.fromTry(Try("something".toInt))
  // backwards
  aValidValue.toEither
  aValidValue.toOption
  aValidValue.toList

  // TODO 2: form validation
  object FormValidation {
    type Form = Map[String, String]
    type FormValidation[T] = Validated[List[String], T]

    /*
      fields are
      - name
      - email
      - password

      rules are
      - name, email and password MUST be specified
      - name must not be blank
      - email must have @
      - password must have at least 10 characters
     */
    def validateForm(form: Form): FormValidation[String] = {
      import cats.syntax.semigroup._
      import cats.data.Validated._

      def nonBlank(fieldName: String, value: String): FormValidation[String] =
        Validated.cond(!value.isBlank, value, List(s"$fieldName must not be blank"))

      def validEmail(email: String): FormValidation[String] =
        Validated.cond(email.contains("@"), email, List("email must have @"))

      def validPassword(password: String): FormValidation[String] =
        Validated.cond(password.length >= 10, password, List("password must have at least 10 characters"))

      def getField(fieldName: String): FormValidation[String] =
        Validated.fromOption(form.get(fieldName), List(s"$fieldName must be specified")).andThen(nonBlank(fieldName, _))

      //val nameCheck = Validated.fromOption(form.get("name"), List("name must be specified"))
      //  .ensure(List("name must not be blank"))(!_.isBlank)

      //val emailCheck = Validated.fromOption(form.get("email"), List("email must be specified"))
      //  .ensure(List("email must have @"))(_.contains('@'))

      //val passwordCheck = Validated.fromOption(form.get("password"), List("password must be specified"))
      //  .ensure(List("password must have at least 10 characters"))(_.length >= 10)

      val nameCheck: FormValidation[String] = getField("name").andThen(nonBlank("name", _))
      val emailCheck = getField("email").andThen(validEmail)
      val passwordCheck = getField("password").andThen(validPassword)

      (nameCheck |+| emailCheck |+| passwordCheck).map(_ => "Success")
    }
  }

  def runTodo2(): Unit = {
    import FormValidation._
    val form: Form = Map(
      "name" -> "Daniel",
      "email" -> "daniel@rtjvm.com",
      "password" -> "Rockthejvm1!"
    )

    println(validateForm(form))
    println()
  }

  // syntax
  import cats.syntax.validated._
  val aValidMeaningOfLife: Validated[List[String], Int] = 42.valid[List[String]]
  val anError: Validated[String, Int] = "Something went wrong".invalid[Int]

  def main(args: Array[String]): Unit = {
    runTodo()

    runTodo2()

    println(aValidMeaningOfLife)
    println(anError)
  }
}
