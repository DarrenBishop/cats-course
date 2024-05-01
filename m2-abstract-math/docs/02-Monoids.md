# Monoids

## Higher-kinded type class that extends `Semigroup` and provides:
 - an `empty` method to produce a _zero_ value
   ```scala mdoc
   import cats.Monoid
   //import cats.instances.int._
   
   val naturalIntMonoid = Monoid[Int] // fetches the implicit instance
   val anIntCombination = naturalIntMonoid.combine(2, 45) // same combine from Semigroup
   val zero = naturalIntMonoid.empty // fundamental to Monoid
   ```

- an `|+|` operator extension method, from `Semigroup`
  ```scala mdoc
  import cats.syntax.monoid._ // includes everything in the `Semigroup` syntax
  val anotherIntCombination = 2 |+| 45 |+| naturalIntMonoid.empty // dictated by the Monoid[Int]
  ```

## Useful for general APIs

```scala mdoc
def empty[T](implicit M: Monoid[T]): T = M.empty // convenience syntax
val yetAnotherIntCombination = 2 |+| 45 |+| empty[Int] // dictated by the Monoid[Int]
```

```scala mdoc
def combineFold[T: Monoid](list: List[T]): T = list.fold(empty)(_ |+| _)
```

## Use cases: data structures meant to be combined
### ...with a starting value
 - data integration & big data processing
 - eventual consistency & distributed computing
