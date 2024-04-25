# Monad Transformers

## Higher-kinded types for convenience over nested monadic values

### `OptionT` Transformer for manadic valeus of Option instances
```scala
import cats.data.OptionT
import cats.instances.list._

val listOfNumberOptions: OptionT[List, Int] = OptionT(List(Option(1), Option(2)))
val listOfCharOptions: OptionT[List, Char] = OptionT(List(Option('a'), Option('b')))
val listOdTuples: OptionT[List, (Int, Char)] = for {
  char <- listOfCharOptions
  number <- listOfNumberOptions
} yield (number, char)
```
 - wrapper over `List[Option[Int]`
 - can access `map`/`flatMap` with an implicit `Monad[List]` in scope

### `EitherT` Transformer for manadic values of Either instances
```scala
import cats.data.EitherT

// apply factor method
val listEithers: EitherT[List, String, Int] = EitherT(List(Left("error"), Right(43), Right(2)))

// alternative: `left` and `right` smart constructors
val futureEither: EitherT[Future, String, Int] = EitherT.right(Future(45))

// change the deeply nested Either
val transformed: EitherT[List, String, Int] = futureEither.transform {
  case Left(undesirable) => // another Either
  case Right(desirable) => // another Either
}
```
