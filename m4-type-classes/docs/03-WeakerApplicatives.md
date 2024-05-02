# Weaker Applicatives

## Higher-kinded type class that extends `Functor` and `Semigroupal`; provides an `ap` method:
 - an `ap` method to apply a function in a context to a value in a context
   ```scala mdoc
   def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]
   ```
   e.g.
   ```scala mdoc
   import cats.Applicative
   //import cats.instances.option._ // implicit Applicative[Option]
   val optionApplicative = Applicative[Option]
   val aMappedOption = optionApplicative.ap(Some((x: Int) => x + 1))(Some(2))
   ```

## Convenient for extracting and combining tuples
```scala mdoc
import cats.syntax.apply._ // extension methods from Apply
val tupleOfOptions = (Option(1), Option(2), Option(3))
val optionOfTuple = tupleOfOptions.tupled
val sumOption = tupleOfOptions.mapN(_ + _ + _)
val anotherTupleOfOptions = (Option(1), Option(2), none[Int])
val multiplyOption = anotherTupleOfOptions.mapN(_ * _ * _)
```

## Use cases: Validation completion
- Use `mapN` over the tuple of `Validated` values to construct something in the success case
