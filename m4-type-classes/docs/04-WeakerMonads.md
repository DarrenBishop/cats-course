# Weaker Monads

## Higher-kinded type class that extends `Apply`; provides the `flatMap` method:
 - a `flatMap` method to transform monadic values in sequence
   ```scala mdoc
   def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
   ```
   e.g.
   ```scala mdoc
   import cats.FlatMap
   //import cats.instances.option._
   
   val optionFlatMap = FlatMap[Option] // fetches the implicit instance
   val anOption = optionFlatMap.pure(2) // returns an Option[Int]
   val aTransformedOption = optionFlatMap.flatMap(anOption) { x =>
     if (x % 2 == 0) Option(x + 1) else None
   } // returns Some(3)
   ```

## `Monad` does not have its own (fundamental) new methods
   ```scala mdoc
   trait Monad[F[_]] extends FlatMap[F] with Applicative[F] 
   ```
## `FlatMap` extends `Apply`, which in turn extends `Functor`...
   ```scala mdoc
   trait Monad[F[_]] extends FlatMap[F] with Applicative[F] {
     // inherits `flatMap` from `FlatMap`
     // inherits `pure`  from `Applicative`
     // inherits `ap` from `Apply` via `Applicative`/`FlatMap`
     // inherits `map` from `Functor` via `Apply`
     // inherits `product` from `Semigroupal` via `Apply`
   }
   ```
e.g.
   ```scala mdoc
   import cats.syntax.flatMap._ // flatMap extension method
   import cats.syntax.functor._ // map extension method
   
   def getPairs[M[_]: FlatMap, A, B](ma: M[A], mb: M[B]): M[(A, B)] = for {
      a <- ma
      b <- mb
   } yield (a, b)
   ```
