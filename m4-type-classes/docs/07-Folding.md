# Foldable

## Higher-kinded type class ; provides _folding_ method:
 - a `foldLeft` method to combine elements in sequence
   ```scala
   def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B
   ```
   e.g.
   ```scala mdoc
   import cats.Foldable
   //import cats.instances.list._
   
   val listFoldable = Foldable[List] // fetches the implicit instance
   val aList = List(1, 2, 3, 4, 5)
   val summedList = listFoldable.foldLeft(aList, 0) { (acc, x) => acc + x }
   ```
 - a `foldRight` method which is stack-safe, regardless of the container
   ```scala
   def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B]
   ```
   e.g.
   ```scala mdoc
   import cats.Eval
   //import cats.instances.vector._
   
   val vectorFoldable = Foldable[Vector] // fetches the implicit instance
   val aVector = Vector(1, 2, 3, 4, 5)
   val summedVector = vectorFoldable.foldRight(aVector, Eval.now(0)) { (x, acc) => acc.map(_ + x) }.value
   ```

## Useful for general APIs applicable to any foldable container

## Extension methods applicable to any foldable container

   ```scala mdoc
   import cats.syntax.foldable._ // foldable extension methods
   val sum3 = List(1, 2, 3).combineAll // requires implicit Foldable[List] and Monoid[Int]
   val mapped = List(1, 2, 3).foldMap(_.toString) // requires implicit Foldable[List] and Monoid[String]
   ```
