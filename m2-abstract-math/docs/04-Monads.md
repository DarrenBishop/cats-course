# Monads

## Higher-kinded type class that provides:
 - a `pure` method to wrap (lift) a normal value into a contextual value, via `Applicative`
 - a `flatMap` method to transform monadic values in sequence
   ```scala mdoc
   import cats.Monad
   //import cats.instances.option._
   
   val optionMonad = Monad[Option] // fetches the implicit instance
   val anOption = optionMonad.pure(2) // returns an Option[Int]
   val aTransformedOption = optionMonad.flatMap(anOption) { x =>
     if (x % 2 == 0) Option(x + 1) else None
   } // returns Some(3)
   ```

## Can implement `map` in terms of `pure` and `flatMap`
 - `Monad` extends `Functor`
 - Spoiler: `Monad` also extends `Applicative`; covered later

## Extension methods are in other packages
   ```scala mdoc
   import cats.syntax.applicative._ // adds the pure extension method
   type ErrorOr[A] = Either[String, A]
   val oneValid = 1.pure[ErrorOr] // returns Right(1)
   ```

   ```scala mdoc
   import cats.syntax.functor._ // adds the map extension method
   val twoValid = oneValid.map(_ + 1) // returns Right(2)
   ```

   ```scala mdoc
   import cats.syntax.monad._ // adds the flatMap extension method
   val transformedValid = twoValid.flatMap(x => (x + 1).pure[ErrorOr]) // returns Right(3)
   ```

## `map` + `flatMap` = for-comprehension
   ```scala mdoc
   val composedErrorOr = for {
     one <- oneValid
     two <- twoValid
   } yield one + two // returns Right(3)
   ```

## Use cases: dependent (sequential) transformations
 - list combinations
 - option transformations
 - chained asynchronous operations

<div style="text-align:center; width:50%; margin:auto;">

> #### For-comprehensions are syntactic sugar for `flatMap` and `map` calls.

> #### For-comprehensions are _NOT ITERATION_.

> #### FlatMap is a mental model of _chained transformations_.
</div>

<div style="text-align:left; width:50%; margin:auto;">

   ```scala mdoc
   import cats.syntax.flatMap._ // adds the flatMap extension method
   import cats.syntax.functor._ // adds the map extension method
   def getPairs[M[_]: Monad, A, B](ma: M[A], mb: M[B]): M[(A, B)] = for {
     a <- ma
     b <- mb
   } yield (a, b)
   ```
</div>
