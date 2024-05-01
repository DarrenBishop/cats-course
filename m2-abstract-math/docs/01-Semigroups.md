# Semigroups

## Type class that can combine values; provides:
 - a `combine` method over two values of the same type and preserving that type
   ```scala mdoc
   import cats.Semigroup
   import cats.instances.int._
   
   val naturalIntSemigroup = Semigroup[Int] // fetches the implicit instance
   val anIntCombination = naturalIntSemigroup.combine(2, 45) // the fundamental API
   ```
 - an `|+|` operator extension method
   ```scala mdoc
   import cats.syntax.semigroup._ // adds the |+| extension method
   val anotherIntCombination = 2 |+| 45 // dictated by the Semigroup[Int]
   ```

## Useful for general APIs
```scala mdoc
def reduceList[T: Semigroup](list: List[T]): T = list.reduce(_ |+| _)
```

## Use cases: data structures meant to be combined
 - data integration & big data processing
 - eventual consistency & distributed computing
