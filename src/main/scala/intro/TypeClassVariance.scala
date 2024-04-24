package intro

import cats.implicits.none

object TypeClassVariance {

  import cats.Eq
  import cats.instances.int._ // Eq[Int] TC instance
  import cats.instances.option._ // Eq[Option[Int]] TC instance
  import cats.syntax.eq._

  val aComparison = Option(2) === Option(3)
  //val anInvalidComparison = Some(2) === None // Eq[Some[Int]] not found

  // variance
  class Animal
  class Cat extends Animal

  // covariant type: subtyping is propagated to the generic type
  class Cage[+T]
  val cage: Cage[Animal] = new Cage[Cat]

  // contravariant type: subtyping is propagated BACKWARDS to the generic type
  class Vet[-T]
  val vet: Vet[Cat] = new Vet[Animal]

  // rule of thumb: 'HAS a T" = covariant, 'ACTS on T' = contravariant
  // variance affects how TC instances are being fetched

  // contravariant TC
  trait SoundMaker[-T]
  implicit object AnimalSoundMaker extends SoundMaker[Animal]
  def makeSound[T](implicit soundMaker: SoundMaker[T]): Unit = println("sound!") // implementation not important
  makeSound[Animal] // ok - AnimalSoundMaker TC instance defined above
  makeSound[Cat] // ok - AnimalSoundMaker TC instance for Animal is a SoundMaker for Cat
  // rule 1: contravariant TCs can use the superclass instances if nothing available strictly for the actual type

  // has implications for subtypes
  implicit object OptionSoundMaker extends SoundMaker[Option[Int]]
  makeSound[Option[Int]] // ok - OptionSoundMaker TC instance defined above
  makeSound[Some[Int]] // ok - OptionSoundMaker TC instance for Option[Int] is a SoundMaker for Some[Int]

  // covariant TC
  trait AnimalShow[+T] {
    def show: String
  }
  implicit object GeneralAnimalShow extends AnimalShow[Animal] {
    override def show: String = "animals everywhere"
  }
  implicit object CatShow extends AnimalShow[Cat] {
    override def show: String = "so many cats!"
  }
  def organizeShow[T](implicit event: AnimalShow[T]): String = event.show
  // rule 2: covariant TCs will always use the more specific TC instance for that type
  // but may confuse the compiler if the general TC is also present

  // rule 3: you can't have both benefits
  // Cats uses INVARIANT TCs
  println(Option(2) === none)

  def main(args: Array[String]): Unit = {
    println(organizeShow[Cat]) // ok - the compiler will inject CatsShow as implicit
    //println(organizeShow[Animal]) // boom! no compile
  }
}
