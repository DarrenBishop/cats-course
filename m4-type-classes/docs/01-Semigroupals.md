# Semigroupals

## Higher-kinded type class that can tuple elements; provides:
 - a `product` method over two values of the same type and preserving that type
   ```scala mdoc
   import cats.Semigroupal
   //import cats.instances.option._ // implicit Semigroupal[Option]
   val optionSemigroupal = Semigroupal[Option]
   val aTupledOption = optionSemigroupal.product(Some(123), Some("a string"))
   ```
   
## Monads extend Semigroupals
`product` is implemented in terms of `flatMap` and `map`

## Some Semigroupals are useful without being Monads
e.g. `Validated` for accumulating errors rather than short-circuiting

## Don;t confuse Semigroup with Semigroupal
Combing vs Tupling:
 - `Semigroup` _combines_ values of the same type
 - `Semigroupal` _tuples_ values of different types
