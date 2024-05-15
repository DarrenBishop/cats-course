package rtj.recap

object TypeClasses {

  case class Person(name: String, age: Int)

  // part 1 - type class definition
  trait JsonSerializer[T] {
    def toJson(value: T): String
  }

  // part 2 - create implicit type class INSTANCES
  implicit object StringSerializer extends JsonSerializer[String] {
    override def toJson(value: String): String = s""""$value""""
  }

  implicit object IntSerializer extends JsonSerializer[Int] {
    override def toJson(value: Int): String = value.toString
  }

  implicit object PersonSerializer extends JsonSerializer[Person] {
    override def toJson(value: Person): String =
      s"""{
         |  "name": "${value.name}",
         |  "age": ${value.age}
         |}""".stripMargin
  }

  // part 3 - offer some API
  def convertListToJson[T](list: List[T])(implicit serializer: JsonSerializer[T]): String =
    list.map(v => serializer.toJson(v)).mkString("[", ", ", "]")

  // part 4 - extending the existing types via extension methods
  object JsonSyntax {
    implicit class JsonSerializable[T](value: T)(implicit serializer: JsonSerializer[T]) {
      def toJson: String = serializer.toJson(value)
    }
  }

  def main(args: Array[String]): Unit = {
    val persons = List(Person("Alice", 23), Person("Xavier", 45))
    println(convertListToJson(persons))

    val strings = List("Alice", "Bob")
    println(convertListToJson(strings))

    val ints = List(1, 2, 3)
    println(convertListToJson(ints))

    import JsonSyntax._

    println(persons.head.toJson)
    println(strings.head.toJson)
    println(ints.head.toJson)

    implicit def listJsonSerializer[T](implicit serializer: JsonSerializer[T]): JsonSerializer[List[T]] =
      convertListToJson

    println(persons.toJson)
    println(strings.toJson)
    println(ints.toJson)
  }
}
