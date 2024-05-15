package rtj.recap

object Implicits {
  case class Person(name: String) {
    def greet: String = s"Hi, my name is $name"
  }

  implicit class ImpersonableString(name: String) {
    def greet: String = Person(name).greet
  }

  val greeting = "Peter".greet // new ImpersonableString("Peter").greet

  // importing implicit conversions in scope
  import scala.concurrent.duration._
  val oneSec = 1.second

  // implicit arguments and values
  def increment(x: Int)(implicit amount: Int) = x + amount
  implicit val defaultAmount = 10

  val incremented2 = increment(2) // implicit argument 10 is passed by the compiler

  def multiply(x: Int)(implicit times: Int) = x * times
  val times2 = multiply(2)

  // more complex example
  trait JsonSerializer[T] {
    def toJson(value: T): String
  }

  def listToJson[T](list: List[T])(implicit serializer: JsonSerializer[T]): String =
    list.map(value => serializer.toJson(value)).mkString("[", ", ", "]")

  implicit val personSerializer: JsonSerializer[Person] =
    (person: Person) =>
      s"""{"name": "${person.name}"}"""

  private val persons: List[Person] = List(Person("Alice"), Person("Bob"))
  // implicit argument is used to PROVE THE EXISTENCE of a type
  val personsJson = listToJson(persons)

  // implicit methods
  implicit def oneArgCaseClassSerializer[T <: Product]: JsonSerializer[T] =
    (value: T) =>
      s"""{"${value.productElementName(0)}": "${value.productElement(0)}"}"""

  implicit def listSerializer[T: JsonSerializer]: JsonSerializer[List[T]] =
    (list: List[T]) => list.map(value => implicitly[JsonSerializer[T]].toJson(value)).mkString("[", ", ", "]")

  case class Cat(name: String)
  val catsToJson = listToJson(List(Cat("Tom"), Cat("Garfield")))
  // in the background: val catsToJson = listToJson(List(Cat("Tom"), Cat("Garfield")))(oneArgCaseClassSerializer)
  // implicit methods are used to PROVE THE EXISTENCE of a type
  // can be used for implicit conversions (DISCOURAGED)

  implicit class JsonOps[T: JsonSerializer](value: T) {
    def toJson: String = implicitly[JsonSerializer[T]].toJson(value)
  }

  def main(args: Array[String]): Unit = {
    println(personSerializer.toJson(Person("David")))
    println(oneArgCaseClassSerializer[Cat].toJson(Cat("Garfield")))
    println(oneArgCaseClassSerializer[Person].toJson(Person("David")))
    println(catsToJson)
    println(persons.toJson)
  }
}
